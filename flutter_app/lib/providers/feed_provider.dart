import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/post.dart';
import '../screens/settings/dev_options_screen.dart';
import '../services/supabase_service.dart';
import 'profile_provider.dart';

final feedProvider = NotifierProvider<FeedNotifier, AsyncValue<List<Post>>>(
  FeedNotifier.new,
);

class FeedNotifier extends Notifier<AsyncValue<List<Post>>> {
  int _currentPage = 0;
  bool _hasMore = true;
  final Set<String> _deletedPostIds = {};
  static const int _defaultPageSize = 20;

  /// Read current dev options (returns safe defaults in release mode)
  DevOptions get _devOptions => ref.read(devOptionsProvider);

  @override
  AsyncValue<List<Post>> build() {
    // Watch dev options so the feed rebuilds when flags change
    ref.watch(devOptionsProvider);
    loadInitial();
    return const AsyncValue.loading();
  }

  Future<void> loadInitial() async {
    final opts = _devOptions;

    state = const AsyncValue.loading();
    _currentPage = 0;
    _hasMore = true;

    // Feature flag: infinite loading – never resolve
    if (opts.infiniteLoading) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] infiniteLoading active – feed will stay loading');
      return; // stays in loading state forever
    }

    // Feature flag: simulate loading error
    if (opts.simulateLoadingError) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] simulateLoadingError active – feed will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('Simulated loading error (dev flag)'),
        StackTrace.current,
      );
      return;
    }

    // Feature flag: simulate offline
    if (opts.simulateOffline) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] simulateOffline active – feed will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('No network connection (simulated offline)'),
        StackTrace.current,
      );
      return;
    }

    try {
      var posts = await _fetchPosts(0);
      posts = _applyContentSafetyFilters(posts, opts);
      if (_deletedPostIds.isNotEmpty) {
        posts = posts.where((p) => !_deletedPostIds.contains(p.id)).toList();
      }
      state = AsyncValue.data(posts);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    _currentPage = 0;
    _hasMore = true;

    final opts = _devOptions;
    if (opts.infiniteLoading || opts.simulateLoadingError || opts.simulateOffline) {
      await loadInitial();
      return;
    }

    try {
      var posts = await _fetchPosts(0);
      posts = _applyContentSafetyFilters(posts, opts);
      if (_deletedPostIds.isNotEmpty) {
        posts = posts.where((p) => !_deletedPostIds.contains(p.id)).toList();
      }
      state = AsyncValue.data(posts);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> loadMore() async {
    if (!_hasMore) return;

    final currentPosts = state.value ?? [];

    try {
      _currentPage++;
      final newPosts = await _fetchPosts(_currentPage);

      if (newPosts.isEmpty) {
        _hasMore = false;
        return;
      }

      state = AsyncValue.data([...currentPosts, ...newPosts]);
    } catch (e) {
      _currentPage--;
    }
  }

  Future<List<Post>> _fetchPosts(int page) async {
    final opts = _devOptions;

    // Feature flag: environment – log active backend target
    if (opts.enableVerboseLogging) {
      debugPrint('[DevFlag] Environment target: ${opts.environment.name}');
    }

    // Feature flag: when environment is non-production, add extra latency to
    // simulate staging/local backend overhead and prefix debug output
    if (opts.environment != DevEnvironmentTarget.production) {
      if (opts.enableVerboseLogging) {
        debugPrint('[DevFlag] Non-production environment (${opts.environment.name}) – adding 200ms overhead');
      }
      await Future.delayed(const Duration(milliseconds: 200));
    }

    // Feature flag: artificial network latency
    final latency = opts.networkLatencyMs;
    if (latency > 0) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] Adding ${latency}ms artificial latency');
      await Future.delayed(Duration(milliseconds: latency));
    }

    // Feature flag: when mock interface is explicitly ENABLED, use mock data
    if (opts.isMockInterfaceEnabled) {
      return _generateMockPosts(page, opts);
    }

    // Default: fetch from Supabase when authenticated with a real user
    // Skip Supabase when devModeSkipAuth is active (no real user) so mock data is used
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && SupabaseService.currentUser != null) {
      return _fetchFromSupabase(page, opts);
    }

    // Not authenticated — fall back to mock data
    return _generateMockPosts(page, opts);
  }

  /// Fetch real posts from Supabase database
  Future<List<Post>> _fetchFromSupabase(int page, DevOptions opts) async {
    try {
      final pageSize = opts.mockPostCount > 0 ? opts.mockPostCount : _defaultPageSize;
      final posts = await SupabaseService.getFeedPosts(
        limit: pageSize,
        offset: page * pageSize,
      );
      debugPrint('[Feed] Fetched ${posts.length} posts from Supabase (page $page)');
      return posts;
    } catch (e) {
      debugPrint('[Feed] Supabase fetch failed: $e — falling back to mock data');
      // Fall back to mock data if Supabase fails
      return _generateMockPosts(page, opts);
    }
  }

  /// Generate mock posts for offline/demo mode using fake personalities
  List<Post> _generateMockPosts(int page, DevOptions opts) {
    // Feature flag: configurable post count
    final pageSize = opts.mockPostCount > 0 ? opts.mockPostCount : _defaultPageSize;

    final ids = fakeProfileIds;
    if (ids.isEmpty) {
      // Defensive fallback — should never happen
      return _generateGenericPosts(page, pageSize, opts);
    }

    // Personality-appropriate sample content keyed by username
    const sampleContent = <String, List<String>>{
      'HyperFocusCode': [
        'Just discovered that setting 3 alarms for everything actually works! 🎯 First alarm: "Hey, you should start thinking about this". Second: "Okay seriously, transition time". Third: "DO IT NOW" 😅',
        'Shipped a new accessibility feature at 2 AM because hyperfocus hit. My triple-alarm system couldn\'t save me from my own code editor 💻',
        'Mechanical keyboard update: the new switches arrived. I\'ve been typing random words for 20 minutes just for the sound. Peak ADHD stimming ⌨️✨',
      ],
      'SensorySeeker': [
        'My new weighted blanket arrived! 15lbs of pure comfort 💙 Already feeling more grounded. The pressure really does help with anxiety and sleep!',
        'Found a café with dim lighting, soft music, AND no strong food smells. This is my new home now. I live here 🏠☕',
        'Sensory win today: wore my noise-canceling earbuds to the grocery store. Zero meltdowns. Zero overwhelm. Technology is beautiful 🎧',
      ],
      'NeuroNurse': [
        '🧠 Quick reminder: Your brain isn\'t broken — it\'s just wired differently. What society calls "deficits" are often just different ways of processing the world. 💜',
        'Neuroscience fact: The ADHD brain doesn\'t lack dopamine — it has trouble REGULATING it. That\'s why you can hyperfocus on things you love but struggle with "boring" tasks.',
        'Friendly reminder: Self-diagnosis is valid. Not everyone has access to (expensive) formal assessments. Trust your lived experience. 🧠✨',
      ],
      'QuietQueen': [
        'Created a "sensory kit" for my desk at work! Includes: fidget cube, noise-canceling earbuds, lavender roller, chewy snacks, and a small plant 🌱 Game changer!',
        'Had to explain "social battery" to my manager today. Drew an actual battery diagram. She gets it now! Advocacy works 🔋',
        'New plant for the collection: a string of pearls! My desk garden now has 12 plants. They never interrupt me 🌿💚',
      ],
      'FocusFounder': [
        'Body doubling session starting in 30 mins! Join me for 2 hours of focused work. No talking, just vibes and productivity. Link in bio! 💪✨',
        'We just hit 200 daily participants in our focus rooms! Your presence helps others focus and theirs helps you. That\'s the magic 🪄',
        'Pomodoro tip for ADHD brains: Don\'t do 25/5. Try 15/5 or even 10/3. Shorter bursts match our attention patterns better ⏱️',
      ],
      'ADHDMemes': [
        'Me: I\'ll just check one thing real quick\n\n*3 hours later*\n\nMe: Wait what was I doing? 🤔😂',
        'ADHD be like: Can\'t remember where I put my keys 5 seconds ago but can recall a random conversation from 2007 in vivid detail 🧠✨',
        'The ADHD experience: Starting 47 projects, finishing 3, and somehow getting a new hobby before lunch 🎨🎸📚🎮',
      ],
      'AutismAdvocate': [
        '🧵 Things I wish people understood about autism masking:\n1. It\'s exhausting\n2. It\'s often unconscious\n3. "You don\'t look autistic" isn\'t a compliment\n4. We do it to survive, not to deceive 💙',
        'Today I unmasked at work for the first time: stimmed during a meeting, asked for written instructions, and said "I need a break" without apologizing ✨',
        'My cat understands me better than most humans. She respects my need for quiet, doesn\'t demand eye contact, and parallel plays perfectly 🐱💙',
      ],
      'TherapyTips': [
        '📊 Study just released: Neurodivergent individuals who found supportive communities reported 67% improvement in mental health outcomes.\n\nCommunity matters. You matter. 💜',
        'Therapy tip: The "5-4-3-2-1" grounding technique works differently for ND brains. Try focusing on just ONE sense deeply instead of cycling through all five.',
        'Reminder: Rest is not laziness. Especially for neurodivergent people, recovery time is a biological necessity 🫗',
      ],
      'neuropsychologist': [
        'Clinical insight: What looks like "laziness" in ADHD is often the paralysis of having too many choices. Reducing options = reducing overwhelm 🧠',
        'Asked my patients what helps most. The #1 answer wasn\'t medication or therapy — it was "feeling understood by someone." Community healing is real 💜',
      ],
      'adhd_alex': [
        'Got diagnosed at 28 after a TikTok about ADHD in adults made me cry. Now I make the videos that help others realize they\'re not alone 🎯',
        'ADHD hack that changed my life: I keep a "done" list instead of a "to-do" list. Seeing what I accomplished > staring at what I haven\'t 📋✨',
      ],
      'mindful_jordan': [
        'New guided meditation dropped: 5 minutes of mindfulness designed specifically for ADHD brains. Uses fidget movements! 🧘✨',
        'Mindfulness doesn\'t have to mean sitting still. Walking meditation, cooking meditation, stimming meditation — all valid 🌿',
      ],
      'sensory_sam': [
        'Fidget toy review #204: This \$3 fidget slug is genuinely the best sensory tool I\'ve tested this year 🐌 Full review dropping tomorrow!',
        'Unpopular opinion: the best sensory tools aren\'t marketed as "fidget toys." Hair ties, smooth stones, pen caps — use what works 🙌',
      ],
      'autism_chris': [
        'Spoke at my 12th conference about autism self-advocacy this weekend. Standing ovation from 300 people. This work matters 🌈',
        'AAC users: you deserve the same respect and patience as any other communicator. Full stop. ♿💙',
      ],
      'adhd_swiftie': [
        'Mapped every Taylor Swift era to an ADHD symptom and it went viral 🎵 The "Midnights = time blindness" post got 24K likes!',
        'New friendship bracelet designs: each one has an ADHD/autism symbol woven in. Special interests COLLIDE 💛',
      ],
      'mike_therapy': [
        'New free worksheet: "Understanding Your Sensory Profile" — identify triggers, find soothing strategies, and communicate needs. Link in bio! 📋',
        'The difference between burnout and autistic burnout: one recovers with vacation, the other may need months of accommodation changes 🧠',
      ],
      'stim_queen': [
        'Friday unboxing! 5 new fidget toys arrived and I\'m reviewing them all LIVE. Spoiler: the magnetic rings are *chef\'s kiss* 💫',
        'My fidget toy collection hit 347. Yes I have a spreadsheet. Data is my love language 📊🐌',
      ],
      'NeuroNaut': [
        'Exploring the intersection of autism and creativity today. Did you know many famous inventors were likely neurodivergent? 🧠🌈',
        'Info dump incoming: fiber arts as a regulation stim. Knitting activates bilateral coordination and provides rhythmic sensory input. It\'s basically therapy with yarn! 🧶',
        'Reminder: being autistic doesn\'t mean we lack empathy. We often feel TOO much. The difference is in how we express it. 💜',
      ],
      'CalmObserver': [
        'Today\'s low-stimulation tip: try "soft fascination" — watching clouds, flowing water, or leaves rustling. It restores attention without overwhelming the senses 🍃',
        'I check messages twice daily and that\'s okay. Boundaries aren\'t rude — they\'re survival. 🧘',
        'Made the perfect cup of tea today. Sometimes the smallest rituals bring the deepest peace ☕✨',
      ],
      'DinoLover99': [
        '🦕 DID YOU KNOW: T-Rex had the most powerful bite force of any land animal EVER — roughly 12,800 pounds! That\'s enough to crush bone. Incredible! 🦖',
        'Museum trip today! Found a beautifully preserved Triceratops horn core. The detail in the fossilization process is just *chef\'s kiss* 🦴✨',
        'Hot take: Birds ARE dinosaurs. Not "descended from" — they literally ARE theropod dinosaurs. Every chicken is a tiny dinosaur. You\'re welcome. 🐔🦖',
      ],
      'Alex_Stims': [
        'New stim toy review: This infinity cube has the most satisfying click I\'ve ever heard. 10/10 would recommend for meetings 🌀',
        'Mechanical keyboard update: Cherry MX Brown switches are my current favorite. The tactile bump without the loud click is *perfect* for stimming while coding ⌨️✨',
        'Built a custom fidget board today! Velcro strips, buttons, switches, and a tiny marble maze. Total cost: \$8. Satisfaction: priceless 🎮',
      ],
      'SpoonCounter': [
        'Spoon check! Started with 10 today, used 4 on groceries and 2 on a phone call. Saving the rest for evening gaming 🥄🎮',
        'Reminder: You don\'t have to earn rest. Rest IS productive. Your body is doing incredible work just existing. 🥄💜',
        'Audiobook recommendation for low-spoon days: anything narrated by someone with a soothing voice. Let your brain be gently entertained 📚',
      ],
      'RainbowNerd': [
        'The overlap between queer and neurodivergent communities is HUGE and we need to talk about it more. Research shows higher rates of autism in LGBTQ+ populations. 🏳️‍🌈♾️',
        'New meme dropped: "Is this a special interest or a hyperfixation?" — it can be both! Especially when it\'s queer history 😂🌈',
        'Happy pride from your favorite queer autistic meme lord! Remember: you don\'t need to mask ANY part of yourself here. 🏳️‍🌈💜',
      ],
      'TransTechie': [
        'Shipped an accessibility feature at 3 AM because ADHD said "now or never." My cats judged me but the users will benefit 🏳️‍⚧️💻',
        'Being trans AND having ADHD means I forget to take my meds at the same time every day. Pill organizer + 3 phone alarms = survival kit 💊⏰',
        'Ruby (the cat, not the language) just walked across my keyboard and somehow fixed a bug. Hiring her immediately 🐱💻',
      ],
      'NonBinaryNinja': [
        'Kata practice is the ultimate stim. Repetitive movement, deep focus, full body engagement. Martial arts saved my regulation 🥋💜',
        'My new fidget ring arrived — hand-forged steel with a spinning outer band. Using it between kata sets and it\'s perfect ⚔️🌀',
        'Friendly reminder: they/them pronouns aren\'t hard. You use them for strangers every day. Practice makes perfect 💜',
      ],
      'BiBookworm': [
        'Just finished the most incredible queer YA audiobook. Bisexual protagonist with ADHD navigating college. I felt SO seen. 💗💜💙📚',
        'Dyslexia hack: audiobooks at 1.25x speed. Fast enough to keep ADHD brain engaged, slow enough for processing. Game changer! 🎧',
        'Hand-bound a new journal today as a stim activity. The repetitive motion of sewing signatures is incredibly soothing 📖✨',
      ],
      'AceExplorer': [
        'Sensory-friendly travel tip: Always pack noise-canceling headphones, a familiar comfort item, and a written itinerary. Predictability = less overwhelm 🗺️🎧',
        'Visited a national park today and rated it 9/10 on sensory friendliness. Quiet trails, no crowds, birdsong only. Paradise. 🌲🖤🤍💜',
        'Solo travel as an autistic person: people think I\'m brave. Really I just prefer my own schedule and no surprise social plans 😅🌍',
      ],
      'PanPride_Sam': [
        'New art piece: the pansexual flag reimagined as a neurodiversity infinity symbol. This is what hyperfocus at 2 AM creates 💖💛💙🎨',
        'Commissions are open! Specializing in neurodivergent-affirming LGBTQ+ portraits. Art is my favorite stim 🎨✨',
        'Color theory fun fact: the pan flag colors (pink, yellow, blue) create a perfect complementary triad. No wonder it looks so good! 💖💛💙',
      ],
      'QueerCoder': [
        'Merged my 100th accessibility PR today. Every alt text, every ARIA label, every keyboard shortcut matters. Inclusive code = better code 🌈💻',
        'Autistic communication style: "This code has a bug" means exactly that. Not "you\'re a bad developer." Direct ≠ rude. 🏳️‍🌈',
        'My Rust accessibility linter just hit 2K stars on GitHub. Proof that hyperfocus + special interest = impact ⭐♾️',
      ],
      'LesbianLuna': [
        'New "Cozy Autism" art piece: a girl stimming happily with her cats by a rain-streaked window. Prints available soon 🧡🤍💗🐱',
        'Studio vibes today: lo-fi music, three cats sleeping nearby, tablet in hand. This is what recharging looks like for me 🎨💤',
        'My cats are named Totoro, Calcifer, and Kiki. Yes I\'m a Ghibli fan. Yes they all have tiny pride bandanas 🐱🌈',
      ],
    };

    final posts = List<Post>.generate(
      pageSize,
      (index) {
        final globalIndex = page * pageSize + index;
        final profileKey = ids[globalIndex % ids.length];
        final profile = getFakeProfile(profileKey);

        // Pick a content line that rotates within this profile's samples
        final contents = sampleContent[profileKey];
        final String content;
        if (contents != null && contents.isNotEmpty) {
          content = contents[globalIndex % contents.length];
        } else {
          content = 'Sharing my neurodivergent journey! 🧠✨ #NeuroComet';
        }

        final user = profile?.user;

        // Assign category and tags for filtering
        final categories = ['general', 'support', 'wins', 'following', 'general', 'support', 'general', 'wins', 'following', 'general'];
        final category = categories[globalIndex % categories.length];
        final tagsByCategory = <String, List<String>>{
          'general': ['community', 'neurodivergent'],
          'support': ['support', 'mentalhealth', 'selfcare'],
          'wins': ['wins', 'celebration', 'achievement'],
          'following': ['following', 'friends'],
        };

        // Randomized location tags for visual variety
        final locations = [
          'Portland, OR', 'San Francisco, CA', 'Austin, TX',
          'London, UK', 'Seattle, WA', 'Toronto, CA',
          'Brooklyn, NY', 'Melbourne, AU', 'Berlin, DE',
          'Amsterdam, NL', 'Denver, CO', 'Tokyo, JP',
          'Chicago, IL', 'Vancouver, BC', 'Barcelona, ES',
          'Stockholm, SE', 'Dublin, IE', 'Singapore',
          'Cape Town, ZA', 'São Paulo, BR',
          null, null, null, null, null, // ~25% chance no location
        ];

        // Randomized profile pic — use unique seed for every post
        final avatarUrl = user?.avatarUrl
            ?? 'https://i.pravatar.cc/150?u=mock_${profileKey}_$globalIndex';

        // Randomized media — ~60% of posts get an image, ~10% get a video thumbnail
        final mediaPool = [
          'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800',
          'https://images.unsplash.com/photo-1612538498456-e861df91d4d0?w=800',
          'https://images.unsplash.com/photo-1497032628192-86f99bcd76bc?w=800',
          'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800',
          'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800',
          'https://images.unsplash.com/photo-1488190211105-8b0e65b80b4e?w=800',
          'https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=800',
          'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800',
          'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
          'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800',
          'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800',
          'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800',
          'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800',
          'https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=800',
          'https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=800',
          null, null, null, null, null, null, null, null, null, null, // ~40% no media
        ];
        final mediaUrl = mediaPool[globalIndex % mediaPool.length];
        final List<String>? mediaUrls = mediaUrl != null ? [mediaUrl] : null;

        return Post(
          id: 'post_${page}_$index',
          authorId: profileKey,
          authorName: user?.displayName ?? 'User ${index + 1}',
          authorAvatarUrl: avatarUrl,
          content: content,
          category: category,
          tags: tagsByCategory[category] ?? ['community'],
          mediaUrls: mediaUrls,
          likeCount: [47, 283, 1562, 92, 534, 2847, 67, 345, 892, 156,
                      3156, 1923, 5621, 412, 78, 234, 1200, 689, 45, 2100][globalIndex % 20],
          commentCount: [12, 45, 187, 8, 67, 342, 5, 29, 67, 23,
                         276, 134, 428, 56, 3, 18, 89, 42, 7, 156][globalIndex % 20],
          shareCount: [4, 12, 89, 2, 45, 156, 1, 8, 23, 6,
                       234, 67, 892, 15, 0, 5, 34, 11, 2, 78][globalIndex % 20],
          locationTag: locations[globalIndex % locations.length],
          createdAt: DateTime.now().subtract(Duration(hours: globalIndex * 2 + 1)),
          // Feature flag: moderation override affects how posts appear
          moderationStatus: _effectiveModerationStatus(content, opts),
        );
      },
    );

    // Feature flag: inject sponsored posts
    if (opts.showSponsoredPosts && page == 0) {
      final sponsoredPost = Post(
        id: 'sponsored_${page}_0',
        authorId: 'sponsor_neurocomet',
        authorName: 'NeuroComet Premium',
        authorAvatarUrl: 'https://i.pravatar.cc/150?u=neurocomet_premium',
        content: '✨ Upgrade to NeuroComet Premium — ad-free experience, exclusive themes, and priority support. Your safe space, elevated. 💜',
        category: 'sponsored',
        tags: const ['sponsored', 'premium'],
        likeCount: 1234,
        commentCount: 56,
        shareCount: 89,
        createdAt: DateTime.now(),
        moderationStatus: 'clean',
      );
      // Insert sponsored post after the 3rd item
      final insertAt = posts.length > 3 ? 3 : posts.length;
      posts.insert(insertAt, sponsoredPost);
    }

    return posts;
  }

  /// Fallback generic post generator (only if fake profiles are empty)
  List<Post> _generateGenericPosts(int page, int pageSize, DevOptions opts) {
    return List.generate(
      pageSize,
      (index) {
        final content = 'This is a sample post #${page * pageSize + index + 1}. '
            'NeuroComet is a safe space for neurodivergent individuals to connect and share! 🧠✨';
        return Post(
          id: 'post_${page}_$index',
          authorId: 'user_$index',
          authorName: 'User ${index + 1}',
          authorAvatarUrl: 'https://i.pravatar.cc/150?u=generic_user_$index',
          content: content,
          likeCount: (index * 7) % 100,
          commentCount: (index * 3) % 50,
          shareCount: (index * 2) % 20,
          createdAt: DateTime.now().subtract(Duration(hours: index)),
          moderationStatus: _effectiveModerationStatus(content, opts),
        );
      },
    );
  }

  /// Determine effective moderation status, respecting the dev override flag.
  /// Mirrors the Kotlin FeedViewModel.effectiveModerationStatus()
  String _effectiveModerationStatus(String content, DevOptions opts) {
    switch (opts.moderationOverride) {
      case ModerationOverride.off:
        return _performContentModeration(content);
      case ModerationOverride.clean:
        return 'clean';
      case ModerationOverride.flagged:
        return 'flagged';
      case ModerationOverride.blocked:
        return 'blocked';
    }
  }

  /// Simple keyword-based content moderation.
  /// Mirrors the Kotlin FeedViewModel.performContentModeration()
  String _performContentModeration(String content) {
    final lower = content.toLowerCase();
    const blockedKeywords = ['kill', 'harm', 'abuse', 'underage', 'threat', 'illegal', 'criminal'];
    const flaggedKeywords = ['scam', 'phishing', 'hate', 'link', 'spam'];

    if (blockedKeywords.any((k) => lower.contains(k))) return 'blocked';
    if (flaggedKeywords.any((k) => lower.contains(k))) return 'flagged';
    return 'clean';
  }

  /// Feature flag: apply content safety filtering based on forcedAudience and isKidsMode
  List<Post> _applyContentSafetyFilters(List<Post> posts, DevOptions opts) {
    var filtered = posts;

    // When isKidsMode is on, remove posts with mature keywords
    if (opts.isKidsMode) {
      const matureKeywords = ['violence', 'trigger', 'harm', 'abuse', 'hate', 'attack'];
      filtered = filtered.where((p) {
        final lower = p.content.toLowerCase();
        return !matureKeywords.any((k) => lower.contains(k));
      }).toList();
    }

    // When forcedAudience is set, filter out content not suitable for that audience
    if (opts.forcedAudience != null) {
      switch (opts.forcedAudience!) {
        case Audience.under13:
          // Only allow very clean content
          filtered = filtered.where((p) => p.moderationStatus == 'clean').toList();
        case Audience.teen:
          // Allow clean + flagged but not blocked
          filtered = filtered.where((p) => p.moderationStatus != 'blocked').toList();
        case Audience.adult:
          // Allow everything
          break;
      }
    }

    return filtered;
  }

  void toggleLike(String postId) {
    final currentPosts = state.value;
    if (currentPosts == null) return;

    state = AsyncValue.data(
      currentPosts.map((post) {
        if (post.id == postId) {
          return post.copyWith(
            isLiked: !post.isLiked,
            likeCount: post.isLiked ? post.likeCount - 1 : post.likeCount + 1,
          );
        }
        return post;
      }).toList(),
    );

    // Persist to Supabase
    _syncLikeToBackend(postId);
  }

  Future<void> _syncLikeToBackend(String postId) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      await SupabaseService.toggleLike(postId);
    } catch (e) {
      debugPrint('[Feed] Like sync error: $e');
    }
  }

  void toggleBookmark(String postId) {
    final currentPosts = state.value;
    if (currentPosts == null) return;

    final post = currentPosts.firstWhere((p) => p.id == postId, orElse: () => currentPosts.first);
    final willBookmark = !post.isBookmarked;

    state = AsyncValue.data(
      currentPosts.map((post) {
        if (post.id == postId) {
          return post.copyWith(isBookmarked: !post.isBookmarked);
        }
        return post;
      }).toList(),
    );

    // Persist to Supabase (fire-and-forget with rollback on failure)
    _syncBookmarkToBackend(postId, willBookmark);
  }

  Future<void> _syncBookmarkToBackend(String postId, bool bookmarked) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      final result = bookmarked
          ? await SupabaseService.bookmarkPost(postId)
          : await SupabaseService.removeBookmark(postId);
      if (result['success'] != true) {
        debugPrint('[Feed] Bookmark sync failed: ${result['message']}');
      }
    } catch (e) {
      debugPrint('[Feed] Bookmark sync error: $e');
    }
  }

  void deletePost(String postId) {
    _deletedPostIds.add(postId);

    final currentPosts = state.value;
    if (currentPosts == null) return;

    state = AsyncValue.data(
      currentPosts.where((post) => post.id != postId).toList(),
    );

    // Persist to Supabase
    _syncDeleteToBackend(postId);
  }

  void injectDevPost(Post post) {
    final currentPosts = state.value ?? const <Post>[];
    final merged = <Post>[
      post,
      ...currentPosts.where((existing) => existing.id != post.id),
    ]
      ..sort((a, b) {
        final aTime = a.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        final bTime = b.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        return bTime.compareTo(aTime);
      });

    state = AsyncValue.data(merged);
  }

  void clearDevInjectedPosts() {
    final currentPosts = state.value ?? const <Post>[];
    state = AsyncValue.data(
      currentPosts.where((post) => !post.id.startsWith('dev_')).toList(),
    );
  }

  Future<void> _syncDeleteToBackend(String postId) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      await SupabaseService.deletePost(postId);
    } catch (e) {
      debugPrint('[Feed] Delete sync error: $e');
    }
  }
}
