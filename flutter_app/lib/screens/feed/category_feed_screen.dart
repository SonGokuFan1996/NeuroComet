import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';
import '../../models/post.dart';
import '../../services/supabase_service.dart';
import '../../widgets/post/post_card.dart';

/// Category Feed Screen - Displays posts filtered by a specific category/community.
/// Mirrors the Kotlin CategoryFeedScreen.kt
class CategoryFeedScreen extends ConsumerStatefulWidget {
  final String categoryName;

  const CategoryFeedScreen({
    super.key,
    required this.categoryName,
  });

  @override
  ConsumerState<CategoryFeedScreen> createState() => _CategoryFeedScreenState();
}

class _CategoryFeedScreenState extends ConsumerState<CategoryFeedScreen> {
  List<Post>? _posts;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadPosts();
  }

  Future<void> _loadPosts() async {
    setState(() { _isLoading = true; _error = null; });

    // Try Supabase first
    try {
      if (SupabaseService.isInitialized && SupabaseService.isAuthenticated) {
        final posts = await SupabaseService.getFeedPosts(limit: 30);
        // Filter by category if the DB returns a category field
        final filtered = posts.where((p) =>
          p.category?.toLowerCase() == widget.categoryName.toLowerCase()
        ).toList();
        if (filtered.isNotEmpty) {
          setState(() { _posts = filtered; _isLoading = false; });
          return;
        }
        // If no category match, show all posts from Supabase (unfiltered)
        if (posts.isNotEmpty) {
          setState(() { _posts = posts; _isLoading = false; });
          return;
        }
      }
    } catch (e) {
      debugPrint('[CategoryFeed] Supabase fetch failed: $e');
    }

    // Fallback: generate demo content for the category
    setState(() {
      _posts = _createDemoPosts(widget.categoryName);
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.categoryName),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : (_posts == null || _posts!.isEmpty)
                  ? _EmptyFeedMessage(categoryName: widget.categoryName)
                  : RefreshIndicator(
                      onRefresh: _loadPosts,
                      child: ListView.builder(
                        padding: const EdgeInsets.all(8),
                        itemCount: _posts!.length,
                        itemBuilder: (context, index) {
                          return PostCard(
                            post: _posts![index],
                            animationIndex: index,
                          );
                        },
                      ),
                    ),
    );
  }

  List<Post> _createDemoPosts(String category) {
    final Map<String, List<_DemoContent>> categoryContent = {
      'Neurobiology': [
        _DemoContent('NeuroScience101', 'Did you know that the neurodivergent brain often has increased connectivity in certain regions? This can lead to unique strengths like pattern recognition and creative thinking! 🧠✨', 42),
        _DemoContent('BrainFacts', 'New research shows that ADHD brains have different dopamine receptor densities. Understanding this helps us appreciate why certain tasks feel easier or harder for different people.', 38),
        _DemoContent('MindMatters', 'Sensory processing differences in autism are now understood to involve atypical neural gating mechanisms. The brain doesn\'t filter input the same way – that\'s not a deficit, it\'s a difference! 🌈', 56),
        _DemoContent('NeuroDiverse', 'Studies on dyslexia reveal fascinating differences in how the brain processes visual information. Many dyslexic individuals have enhanced spatial reasoning abilities.', 29),
        _DemoContent('ScienceOfND', 'The concept of "neurodiversity" was coined in 1998 by sociologist Judy Singer. It frames neurological differences as natural human variations rather than disorders. 🧩', 67),
      ],
      'ADHD': [
        _DemoContent('HyperFocusCode', 'Setting 3 alarms for everything actually works! 🎯 First: "Think about starting." Second: "Transition time." Third: "DO IT NOW" 😅', 283),
        _DemoContent('ADHDMemes', 'Me: I\'ll just check one thing real quick\n*3 hours later*\nMe: Wait what was I doing? 🤔😂', 1562),
        _DemoContent('FocusFounder', 'Pomodoro tip for ADHD brains: Don\'t do 25/5. Try 15/5 or even 10/3. Shorter bursts match our attention patterns better ⏱️', 534),
        _DemoContent('adhd_alex', 'ADHD hack that changed my life: I keep a "done" list instead of a "to-do" list. Seeing what I accomplished > staring at what I haven\'t 📋✨', 892),
        _DemoContent('adhd_swiftie', 'Mapped every Taylor Swift era to an ADHD symptom and it went viral 🎵 The "Midnights = time blindness" post got 24K likes!', 2100),
      ],
      'Autism': [
        _DemoContent('AutismAdvocate', '🧵 Things I wish people understood about autism masking:\n1. It\'s exhausting\n2. It\'s often unconscious\n3. "You don\'t look autistic" isn\'t a compliment 💙', 3156),
        _DemoContent('NeuroNurse', 'Reminder: being autistic doesn\'t mean we lack empathy. We often feel TOO much. The difference is in how we express it. 💜', 1923),
        _DemoContent('autism_chris', 'Spoke at my 12th conference about autism self-advocacy this weekend. Standing ovation from 300 people. This work matters 🌈', 534),
        _DemoContent('NeuroNaut', 'Info dump incoming: fiber arts as a regulation stim. Knitting activates bilateral coordination and provides rhythmic sensory input. 🧶', 345),
        _DemoContent('CalmObserver', 'Today\'s low-stimulation tip: try "soft fascination" — watching clouds, flowing water, or leaves rustling. It restores attention without overwhelm 🍃', 156),
      ],
      'Sensory': [
        _DemoContent('SensorySeeker', 'Weighted blanket tier list update: The 15lb bamboo fabric one is STILL my #1. Nothing beats that deep pressure input 💙', 892),
        _DemoContent('sensory_sam', 'Fidget toy review #204: This \$3 fidget slug is genuinely the best sensory tool I\'ve tested this year 🐌', 345),
        _DemoContent('QuietQueen', 'Created a "sensory kit" for my desk at work! Includes: fidget cube, noise-canceling earbuds, lavender roller, chewy snacks 🌱', 534),
        _DemoContent('Alex_Stims', 'Built a custom fidget board today! Velcro strips, buttons, switches, and a tiny marble maze. Total cost: \$8. Satisfaction: priceless 🎮', 283),
        _DemoContent('stim_queen', 'My fidget toy collection hit 347. Yes I have a spreadsheet. Data is my love language 📊🐌', 156),
      ],
      'Mental Health': [
        _DemoContent('TherapyTips', '📊 Study just released: Neurodivergent individuals who found supportive communities reported 67% improvement in mental health outcomes. 💜', 2847),
        _DemoContent('mike_therapy', 'The difference between burnout and autistic burnout: one recovers with vacation, the other may need months of accommodation changes 🧠', 1200),
        _DemoContent('mindful_jordan', 'Mindfulness doesn\'t have to mean sitting still. Walking meditation, cooking meditation, stimming meditation — all valid 🌿', 412),
        _DemoContent('SpoonCounter', 'Reminder: You don\'t have to earn rest. Rest IS productive. Your body is doing incredible work just existing 🥄💜', 689),
        _DemoContent('neuropsychologist', 'What looks like "laziness" in ADHD is often the paralysis of having too many choices. Reducing options = reducing overwhelm 🧠', 534),
      ],
      'Accommodations': [
        _DemoContent('QuietQueen', 'Had to explain "social battery" to my manager today. Drew an actual battery diagram. She gets it now! Advocacy works 🔋', 1562),
        _DemoContent('autism_chris', 'AAC users: you deserve the same respect and patience as any other communicator. Full stop. ♿💙', 892),
        _DemoContent('AutismAdvocate', 'Today I unmasked at work for the first time: stimmed during a meeting, asked for written instructions, and said "I need a break" without apologizing ✨', 2100),
        _DemoContent('TransTechie', 'Open sourced my screen reader plugin today! 10K+ users and growing. Accessible tech is a right, not a privilege 🏳️‍⚧️♿', 534),
        _DemoContent('QueerCoder', 'Merged my 100th accessibility PR. Every alt text, every ARIA label, every keyboard shortcut matters. Inclusive code = better code 🌈💻', 345),
      ],
      'Community': [
        _DemoContent('FocusFounder', 'Body doubling session starting in 30 mins! Join me for 2 hours of focused work. No talking, just vibes and productivity 💪✨', 534),
        _DemoContent('RainbowNerd', 'The overlap between queer and neurodivergent communities is HUGE. Research shows higher rates of autism in LGBTQ+ populations 🏳️‍🌈♾️', 1923),
        _DemoContent('NeuroNaut', 'Community highlight: This space has grown so much. Seeing neurodivergent people support each other openly is exactly why I started posting here 🧠🌈', 345),
        _DemoContent('PanPride_Sam', 'Commissions are open! Specializing in neurodivergent-affirming LGBTQ+ portraits. Art is my favorite stim 🎨✨', 283),
        _DemoContent('DinoLover99', '🦕 DID YOU KNOW: T-Rex had the most powerful bite force of any land animal EVER — roughly 12,800 pounds! 🦖', 892),
      ],
      'Wins': [
        _DemoContent('HyperFocusCode', 'Just shipped a new accessibility feature at 2 AM because hyperfocus hit. But hey, users will benefit! 💻', 283),
        _DemoContent('AceExplorer', 'Country #23: Iceland! Sensory rating: 10/10. Wide open spaces, minimal crowds, and the Northern Lights 🇮🇸✨', 534),
        _DemoContent('QueerCoder', 'My Rust accessibility linter just hit 2K stars on GitHub. Proof that hyperfocus + special interest = impact ⭐♾️', 1200),
        _DemoContent('BiBookworm', 'Just finished the most incredible queer YA audiobook. Bisexual protagonist with ADHD navigating college. I felt SO seen 💗💜💙📚', 345),
        _DemoContent('FocusFounder', 'We just hit 200 daily participants in our focus rooms! Your presence helps others focus. That\'s the magic 🪄', 892),
      ],
    };

    final contents = categoryContent[category] ?? categoryContent.values.expand((e) => e).take(5).toList();

    return contents.asMap().entries.map((entry) {
      final i = entry.key;
      final c = entry.value;
      return Post(
        id: 'cat_${category}_$i',
        authorId: c.username,
        authorName: c.username,
        authorAvatarUrl: 'https://i.pravatar.cc/150?u=${Uri.encodeComponent(c.username)}',
        content: c.content,
        category: category.toLowerCase(),
        likeCount: c.likes,
        commentCount: (c.likes * 0.15).toInt(),
        createdAt: DateTime.now().subtract(Duration(hours: i * 3 + 1)),
      );
    }).toList();
  }
}

class _EmptyFeedMessage extends StatelessWidget {
  final String categoryName;

  const _EmptyFeedMessage({required this.categoryName});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: AppColors.primaryPurple.withAlpha(30),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.article_outlined,
                size: 48,
                color: AppColors.primaryPurple,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'No Posts Yet',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Be the first to post in $categoryName!',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _DemoContent {
  final String username;
  final String content;
  final int likes;
  const _DemoContent(this.username, this.content, this.likes);
}

