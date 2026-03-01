import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user.dart';
import '../models/post.dart';
import '../models/custom_avatar.dart';
import '../widgets/profile/neuro_traits.dart';

final currentUserProfileProvider = FutureProvider<User>((ref) async {
  // Replace with actual auth/Supabase call
  await Future.delayed(const Duration(milliseconds: 500));

  return const User(
    id: 'current_user',
    displayName: 'Current User',
    username: 'currentuser',
    email: 'user@example.com',
    avatarUrl: 'https://i.pravatar.cc/150?img=10',
    bio: 'Neurodivergent advocate | ADHD | Autism 🧠✨',
    postCount: 42,
    followerCount: 1234,
    followingCount: 567,
    isPremium: true,
    isVerified: true,
    badges: ['Early Adopter', 'Community Helper'],
  );
});

/// Provider for storing the current user's custom avatar
/// This is persisted locally and synced with the backend
final customAvatarProvider = NotifierProvider<CustomAvatarNotifier, CustomAvatar?>(
  CustomAvatarNotifier.new,
);

class CustomAvatarNotifier extends Notifier<CustomAvatar?> {
  @override
  CustomAvatar? build() => null;

  void setAvatar(CustomAvatar avatar) {
    state = avatar;
  }

  void clearAvatar() {
    state = null;
  }
}

// ---------------------------------------------------------------------------
// Fake profile database — rich data for every personality shown in the app
// ---------------------------------------------------------------------------

/// Complete profile data for a fake user, including traits, interests, etc.
class FakeProfileData {
  final User user;
  final List<NeuroDivergentTrait> traits;
  final List<String> interests;
  final String communicationNotes;
  final String pronouns;
  final EnergyStatus energyStatus;
  final String funFact;
  final String location;
  final String joinedDate;

  const FakeProfileData({
    required this.user,
    required this.traits,
    required this.interests,
    required this.communicationNotes,
    required this.pronouns,
    required this.energyStatus,
    required this.funFact,
    required this.location,
    required this.joinedDate,
  });
}

/// Central fake-profile map keyed by userId.
/// Any user ID that isn't found falls back to generic data.
final Map<String, FakeProfileData> _fakeProfiles = {
  // ── For-You feed personalities ──────────────────────────────────────

  'HyperFocusCode': FakeProfileData(
    user: const User(
      id: 'HyperFocusCode',
      displayName: 'Alex Chen',
      username: 'HyperFocusCode',
      avatarUrl: 'https://i.pravatar.cc/150?u=hyperfocuscode',
      bio: 'Software dev with ADHD 💻 Building tools for brains like mine. '
          'Triple-alarm productivity system inventor. Code & coffee enthusiast ☕',
      postCount: 284,
      followerCount: 12400,
      followingCount: 643,
      isPremium: true,
      isVerified: true,
      badges: ['Top Creator', 'ADHD Advocate', 'Early Adopter', 'Streak Master'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.needsReminders,
      NeuroDivergentTrait.flexibleTiming,
    ],
    interests: ['Coding', 'Mechanical Keyboards', 'Productivity Systems', 'Open Source', 'Coffee Brewing'],
    communicationNotes: 'I prefer async communication — DMs over calls. '
        'I might hyperfocus and reply hours later, it\'s not personal! /gen',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.hyperfocus,
    funFact: 'Once coded for 14 hours straight because I discovered a new Rust crate. '
        'My triple-alarm system was born out of missing 3 meetings in one week 😅',
    location: 'San Francisco, CA',
    joinedDate: 'March 2024',
  ),

  'SensorySeeker': FakeProfileData(
    user: const User(
      id: 'SensorySeeker',
      displayName: 'Jordan Rivera',
      username: 'SensorySeeker',
      avatarUrl: 'https://i.pravatar.cc/150?u=sensoryseeker',
      bio: 'Sensory processing advocate 🌈 Reviewing weighted blankets so you don\'t have to. '
          'SPD + autism. Cozy vibes only 💙',
      postCount: 156,
      followerCount: 8900,
      followingCount: 412,
      isPremium: false,
      isVerified: false,
      badges: ['Sensory Expert', 'Community Helper', '100 Posts'],
    ),
    traits: [
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.textPreferred,
      NeuroDivergentTrait.socialBattery,
    ],
    interests: ['Weighted Blankets', 'Sensory Products', 'Nature Walks', 'Tea Ceremonies', 'ASMR'],
    communicationNotes: 'Please no voice messages or video calls — text only. '
        'I use tone indicators and appreciate them back! /gen /srs',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.lowBattery,
    funFact: 'I own 7 weighted blankets ranging from 10 to 25 lbs. '
        'My cat also has a tiny weighted blanket. Yes, she loves it.',
    location: 'Portland, OR',
    joinedDate: 'May 2024',
  ),

  'NeuroNurse': FakeProfileData(
    user: const User(
      id: 'NeuroNurse',
      displayName: 'Dr. Sam Kim',
      username: 'NeuroNurse',
      avatarUrl: 'https://i.pravatar.cc/150?u=neuronurse',
      bio: '🧠 Neurodiversity educator & clinical researcher. '
          'PhD in Neuroscience. Your brain isn\'t broken — it\'s a feature, not a bug. 💜',
      postCount: 521,
      followerCount: 45600,
      followingCount: 234,
      isPremium: true,
      isVerified: true,
      badges: ['Verified Professional', 'Top Educator', 'Research Pioneer', '500 Posts', 'Community Pillar'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.explicitExpectations,
    ],
    interests: ['Neuroscience', 'Research Papers', 'Public Speaking', 'Accessibility Design', 'Board Games'],
    communicationNotes: 'Happy to answer questions about neurodiversity! '
        'I read DMs weekly. For urgent clinical concerns, please contact a local provider.',
    pronouns: 'he/they',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I can explain the dopamine system using only cooking metaphors. '
        'My most-shared post has been reposted 892 times!',
    location: 'Boston, MA',
    joinedDate: 'January 2024',
  ),

  'QuietQueen': FakeProfileData(
    user: const User(
      id: 'QuietQueen',
      displayName: 'Maya Thompson',
      username: 'QuietQueen',
      avatarUrl: 'https://i.pravatar.cc/150?u=quietqueen',
      bio: 'Building a quieter world, one sensory kit at a time 🌱 '
          'Workplace accommodations advocate. Introvert energy. Plants > people (sometimes) 🌿',
      postCount: 198,
      followerCount: 15200,
      followingCount: 387,
      isPremium: false,
      isVerified: false,
      badges: ['Accommodation Expert', 'Plant Parent', 'Community Helper'],
    ),
    traits: [
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.socialBattery,
      NeuroDivergentTrait.smallGroups,
      NeuroDivergentTrait.routineOriented,
    ],
    interests: ['Sensory Kits', 'Indoor Plants', 'Quiet Cafés', 'Journaling', 'Workplace Design'],
    communicationNotes: 'I\'m an introvert who loves meaningful 1-on-1 chats. '
        'Group chats drain me fast. Best time to reach me: mornings 🌅',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.neutral,
    funFact: 'My desk sensory kit went viral and now 3 companies have asked me to consult '
        'on office design for neurodivergent employees!',
    location: 'Seattle, WA',
    joinedDate: 'February 2024',
  ),

  'FocusFounder': FakeProfileData(
    user: const User(
      id: 'FocusFounder',
      displayName: 'Chris Lee',
      username: 'FocusFounder',
      avatarUrl: 'https://i.pravatar.cc/150?u=focusfounder',
      bio: 'Body-doubling evangelist 💪 ADHD coach & community builder. '
          'Turning parallel work into a movement. Join our daily focus rooms! ✨',
      postCount: 342,
      followerCount: 23100,
      followingCount: 521,
      isPremium: true,
      isVerified: true,
      badges: ['Community Builder', 'ADHD Coach', 'Focus Champion', 'Early Adopter'],
    ),
    traits: [
      NeuroDivergentTrait.parallelPlay,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.needsReminders,
      NeuroDivergentTrait.stimmingPositive,
    ],
    interests: ['Body Doubling', 'Pomodoro Technique', 'Community Building', 'Running', 'Lo-fi Music'],
    communicationNotes: 'Always happy to chat about focus strategies! '
        'I run daily body-doubling sessions at 10 AM and 3 PM EST. All welcome!',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'I\'ve hosted over 500 body-doubling sessions. Our longest streak: '
        '47 days straight with 200+ participants daily! 🏆',
    location: 'Austin, TX',
    joinedDate: 'January 2024',
  ),

  // ── Trending / viral post personalities ────────────────────────────

  'ADHDMemes': FakeProfileData(
    user: const User(
      id: 'ADHDMemes',
      displayName: 'ADHD Meme Central',
      username: 'ADHDMemes',
      avatarUrl: 'https://i.pravatar.cc/150?u=adhdmemes',
      bio: 'Making ADHD relatable, one meme at a time 😂 '
          'Laughing through the executive dysfunction. DMs open for meme submissions!',
      postCount: 1247,
      followerCount: 89400,
      followingCount: 312,
      isPremium: true,
      isVerified: true,
      badges: ['Meme Lord', 'Viral Creator', '1K Posts', 'Community Favorite'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.flexibleTiming,
      NeuroDivergentTrait.stimmingPositive,
    ],
    interests: ['Meme Creation', 'ADHD Advocacy', 'Graphic Design', 'Stand-up Comedy', 'Doom Scrolling (ironic)'],
    communicationNotes: 'DMs open for meme submissions! I post 3-5 times daily. '
        'Collabs welcome. I might respond at 3 AM — time blindness is real 😅',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'Started this account after a meme I made at 2 AM went viral with 50K shares. '
        'Proof that hyperfocus at weird hours pays off!',
    location: 'The Internet',
    joinedDate: 'December 2023',
  ),

  'AutismAdvocate': FakeProfileData(
    user: const User(
      id: 'AutismAdvocate',
      displayName: 'Emma\'s Autism Journey',
      username: 'AutismAdvocate',
      avatarUrl: 'https://i.pravatar.cc/150?u=autismadvocate',
      bio: 'Late-diagnosed autistic woman 💙 Unmasking one day at a time. '
          'Educator, writer & thread queen. Your "you don\'t look autistic" is not a compliment.',
      postCount: 623,
      followerCount: 67800,
      followingCount: 445,
      isPremium: true,
      isVerified: true,
      badges: ['Autism Advocate', 'Thread Queen', 'Awareness Champion', 'Published Author'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.needsProcessingTime,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.sensorySensitive,
    ],
    interests: ['Autism Advocacy', 'Creative Writing', 'Pattern Recognition', 'Cats', 'Historical Fiction'],
    communicationNotes: 'I take masking breaks and may go quiet for days. '
        'I prefer structured conversations with clear topics. Tone indicators always welcome!',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.recharging,
    funFact: 'My thread on autism masking has been shared over 8,700 times. '
        'I\'ve also memorized the entire London Underground map — just because.',
    location: 'London, UK',
    joinedDate: 'November 2023',
  ),

  'TherapyTips': FakeProfileData(
    user: const User(
      id: 'TherapyTips',
      displayName: 'Dr. Mental Health',
      username: 'TherapyTips',
      avatarUrl: 'https://i.pravatar.cc/150?u=therapytips',
      bio: '📊 Evidence-based mental health content for neurodivergent minds. '
          'Licensed psychologist. Community > isolation. Your feelings are valid 💜',
      postCount: 412,
      followerCount: 134000,
      followingCount: 178,
      isPremium: true,
      isVerified: true,
      badges: ['Verified Professional', 'Top Educator', 'Research Sharer', 'Community Pillar', 'Trusted Voice'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.explicitExpectations,
      NeuroDivergentTrait.passionateInterests,
    ],
    interests: ['Clinical Psychology', 'Research', 'Community Mental Health', 'Yoga', 'Cooking'],
    communicationNotes: 'I share research, not diagnoses. For personal support, '
        'please reach out to a local provider. Educational DMs welcome!',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'The community-health study I shared got picked up by 3 major news outlets. '
        'Also: I make a mean sourdough when I\'m not reading papers 🍞',
    location: 'Chicago, IL',
    joinedDate: 'October 2023',
  ),

  // ── Featured creators from People tab ──────────────────────────────

  'neuropsychologist': FakeProfileData(
    user: const User(
      id: 'neuropsychologist',
      displayName: 'Dr. Sarah Chen',
      username: 'neuropsychologist',
      avatarUrl: 'https://i.pravatar.cc/150?u=sarahchen',
      bio: 'Clinical psychologist specializing in ADHD & autism. '
          'Author of "The Neurodivergent Mind". Here to spread knowledge & hope 🧠💜',
      postCount: 387,
      followerCount: 124000,
      followingCount: 298,
      isPremium: true,
      isVerified: true,
      badges: ['Verified Professional', 'Published Author', 'Top Educator', 'Community Pillar'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.explicitExpectations,
      NeuroDivergentTrait.passionateInterests,
    ],
    interests: ['Neuropsychology', 'Writing', 'Patient Advocacy', 'Hiking', 'Tea'],
    communicationNotes: 'I love thoughtful questions! Please note I cannot provide '
        'clinical advice via social media. Book recommendations always welcome 📚',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'My book "The Neurodivergent Mind" started as a Twitter thread '
        'that got 100K likes. The power of info-dumping!',
    location: 'New York, NY',
    joinedDate: 'September 2023',
  ),

  'adhd_alex': FakeProfileData(
    user: const User(
      id: 'adhd_alex',
      displayName: 'Alex Rivera',
      username: 'adhd_alex',
      avatarUrl: 'https://i.pravatar.cc/150?u=alexrivera',
      bio: 'ADHD advocate & content creator 🎯 Late-diagnosed at 28. '
          'Sharing my journey & tips that actually work!',
      postCount: 467,
      followerCount: 89200,
      followingCount: 534,
      isPremium: true,
      isVerified: true,
      badges: ['ADHD Advocate', 'Top Creator', 'Late-Dx Pride', 'Streak Master'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.needsReminders,
      NeuroDivergentTrait.flexibleTiming,
      NeuroDivergentTrait.directCommunicator,
    ],
    interests: ['ADHD Strategies', 'Content Creation', 'Video Editing', 'Skateboarding', 'Cooking'],
    communicationNotes: 'I\'m an open book about ADHD! Ask me anything. '
        'Replies may be delayed — I batch-respond to save executive function.',
    pronouns: 'he/they',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'Got diagnosed at 28 after a TikTok about ADHD in adults made me cry. '
        'Now I make the videos that help others realize they\'re not alone.',
    location: 'Miami, FL',
    joinedDate: 'February 2024',
  ),

  'mindful_jordan': FakeProfileData(
    user: const User(
      id: 'mindful_jordan',
      displayName: 'Jordan Taylor',
      username: 'mindful_jordan',
      avatarUrl: 'https://i.pravatar.cc/150?u=jordantaylor',
      bio: 'Mindfulness coach for neurodivergent adults 🧘 '
          'Making meditation accessible for busy brains',
      postCount: 234,
      followerCount: 45600,
      followingCount: 312,
      isPremium: false,
      isVerified: false,
      badges: ['Mindfulness Guide', 'Accessibility Champion', 'Community Helper'],
    ),
    traits: [
      NeuroDivergentTrait.needsProcessingTime,
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.socialBattery,
    ],
    interests: ['Mindfulness', 'Meditation', 'Yoga', 'Nature Photography', 'Herbal Tea'],
    communicationNotes: 'I respond slowly and intentionally. '
        'No pressure for quick replies here — take your time too 🌿',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.neutral,
    funFact: 'I designed a 5-minute meditation specifically for ADHD brains '
        'that uses fidget movements. It\'s been downloaded 50K+ times!',
    location: 'Boulder, CO',
    joinedDate: 'April 2024',
  ),

  'sensory_sam': FakeProfileData(
    user: const User(
      id: 'sensory_sam',
      displayName: 'Sam Kim',
      username: 'sensory_sam',
      avatarUrl: 'https://i.pravatar.cc/150?u=samkim',
      bio: 'Sensory processing tips & product reviews ✨ '
          'OT student & SPD advocate',
      postCount: 178,
      followerCount: 23100,
      followingCount: 456,
      isPremium: false,
      isVerified: false,
      badges: ['Sensory Expert', 'Product Reviewer', 'OT Student'],
    ),
    traits: [
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.textPreferred,
      NeuroDivergentTrait.needsProcessingTime,
    ],
    interests: ['Sensory Products', 'Occupational Therapy', 'Texture Art', 'Baking', 'Rain Sounds'],
    communicationNotes: 'Text-only please! I review sensory products honestly — '
        'send recommendations, not sponsorships 🙏',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.lowBattery,
    funFact: 'I\'ve tested over 200 fidget toys and my spreadsheet rating them '
        'has become a community resource. Data is my love language 📊',
    location: 'Minneapolis, MN',
    joinedDate: 'June 2024',
  ),

  'autism_chris': FakeProfileData(
    user: const User(
      id: 'autism_chris',
      displayName: 'Chris Morgan',
      username: 'autism_chris',
      avatarUrl: 'https://i.pravatar.cc/150?u=chrismorgan',
      bio: 'Autism self-advocate & speaker 🌈 AAC user. '
          'Late-diagnosed. Proudly autistic!',
      postCount: 356,
      followerCount: 67800,
      followingCount: 289,
      isPremium: true,
      isVerified: true,
      badges: ['Autism Advocate', 'AAC Champion', 'Public Speaker', 'Community Pillar'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.explicitExpectations,
    ],
    interests: ['AAC Technology', 'Public Speaking', 'Disability Rights', 'Train Schedules', 'Pixel Art'],
    communicationNotes: 'I use AAC part-time. Please be patient with response times. '
        'I prefer structured conversations with clear agendas.',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I\'ve spoken at 12 conferences about autism self-advocacy. '
        'Also, I can recite every major train route in North America from memory 🚂',
    location: 'Toronto, Canada',
    joinedDate: 'January 2024',
  ),

  'adhd_swiftie': FakeProfileData(
    user: const User(
      id: 'adhd_swiftie',
      displayName: 'Taylor Swift-Mind',
      username: 'adhd_swiftie',
      avatarUrl: 'https://i.pravatar.cc/150?u=adhdswiftie',
      bio: 'Combining special interests: Taylor Swift + ADHD content 🎵 '
          'Making executive dysfunction eras fun!',
      postCount: 289,
      followerCount: 31400,
      followingCount: 678,
      isPremium: false,
      isVerified: false,
      badges: ['Special Interest Specialist', 'Creative Content', 'Fan Favorite'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.flexibleTiming,
      NeuroDivergentTrait.stimmingPositive,
    ],
    interests: ['Taylor Swift', 'ADHD Advocacy', 'Music Analysis', 'Concert Going', 'Friendship Bracelets'],
    communicationNotes: 'I WILL info-dump about Taylor Swift and ADHD. '
        'You have been warned and you are welcome 💛',
    pronouns: 'she/they',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'I\'ve mapped every Taylor Swift era to an ADHD symptom and it went viral. '
        'The "Midnights = time blindness" post got 24K likes!',
    location: 'Nashville, TN',
    joinedDate: 'March 2024',
  ),

  'mike_therapy': FakeProfileData(
    user: const User(
      id: 'mike_therapy',
      displayName: 'Dr. Mike Therapy',
      username: 'mike_therapy',
      avatarUrl: 'https://i.pravatar.cc/150?u=miketherapy',
      bio: 'Licensed therapist specializing in anxiety & neurodivergence 🎓 '
          'Free resources in bio!',
      postCount: 534,
      followerCount: 156000,
      followingCount: 167,
      isPremium: true,
      isVerified: true,
      badges: ['Verified Professional', 'Resource Creator', 'Top Educator', 'Trusted Voice', '500 Posts'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.explicitExpectations,
      NeuroDivergentTrait.passionateInterests,
    ],
    interests: ['Therapy Techniques', 'Anxiety Management', 'Free Resources', 'Guitar', 'Dad Jokes'],
    communicationNotes: 'I share tools, not diagnoses. My free resource library has 100+ downloads. '
        'For therapy, please find a local provider. Educational questions welcome!',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I started making free therapy worksheets during the pandemic and now '
        'they\'re used in 200+ clinics worldwide. Also, my dad jokes are therapeutic 😄',
    location: 'Denver, CO',
    joinedDate: 'August 2023',
  ),

  'stim_queen': FakeProfileData(
    user: const User(
      id: 'stim_queen',
      displayName: 'Stim Queen',
      username: 'stim_queen',
      avatarUrl: 'https://i.pravatar.cc/150?u=stimqueen',
      bio: 'Stimming is self-care! 💫 '
          'Fidget reviews & stim toy recommendations',
      postCount: 198,
      followerCount: 18900,
      followingCount: 523,
      isPremium: false,
      isVerified: false,
      badges: ['Stim Expert', 'Product Reviewer', 'Community Favorite'],
    ),
    traits: [
      NeuroDivergentTrait.stimmingPositive,
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.infoDumpingWelcome,
    ],
    interests: ['Fidget Toys', 'Stim Videos', 'Craft Making', 'Slime', 'ASMR'],
    communicationNotes: 'Send me your favorite stim toys! I review everything. '
        'Unboxing videos every Friday ✨',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'My fidget toy collection has 347 items and counting. '
        'My most-watched review has 200K views — it was a \$3 fidget slug 🐌',
    location: 'Atlanta, GA',
    joinedDate: 'May 2024',
  ),

  // ── Kotlin-side personalities (DataModels.kt / ProfileScreen.kt) ───

  'NeuroNaut': FakeProfileData(
    user: const User(
      id: 'NeuroNaut',
      displayName: 'NeuroNaut',
      username: 'NeuroNaut',
      avatarUrl: 'https://i.pravatar.cc/150?u=neuronaut',
      bio: 'Exploring the neurodivergent experience one post at a time. '
          'Autistic advocate & community builder. 🧠🌈',
      postCount: 89,
      followerCount: 1250,
      followingCount: 340,
      isPremium: true,
      isVerified: true,
      badges: ['Community Builder', 'Autism Advocate', 'Top Contributor', 'Early Adopter'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.stimmingPositive,
    ],
    interests: ['Neuroscience', 'Advocacy', 'Fiber Arts', 'Star Trek'],
    communicationNotes: "I'm very literal — please say what you mean! "
        "I love info dumps about your interests. 💜",
    pronouns: 'she/her',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I can name every Star Trek episode by its stardate. '
        'Fiber arts are my go-to regulation stim — currently knitting a PADD cozy!',
    location: 'Somewhere quiet',
    joinedDate: 'March 2023',
  ),

  'CalmObserver': FakeProfileData(
    user: const User(
      id: 'CalmObserver',
      displayName: 'CalmObserver',
      username: 'CalmObserver',
      avatarUrl: 'https://i.pravatar.cc/150?u=calmobserver',
      bio: 'Mindfulness practitioner. Finding peace in the chaos. '
          'Low-stimulation lifestyle advocate. 🧘',
      postCount: 45,
      followerCount: 345,
      followingCount: 89,
      isPremium: false,
      isVerified: false,
      badges: ['Mindful Soul', 'Quiet Contributor'],
    ),
    traits: [
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.socialBattery,
      NeuroDivergentTrait.smallGroups,
      NeuroDivergentTrait.routineOriented,
    ],
    interests: ['Meditation', 'Tea', 'Minimalism', 'Nature Photography'],
    communicationNotes: 'I check messages twice daily. '
        'Urgent? Use the 🚨 emoji.',
    pronouns: 'he/they',
    energyStatus: EnergyStatus.recharging,
    funFact: 'I once went 30 days without screens and documented the whole '
        'experience in a hand-written journal. It was incredibly grounding 🍃',
    location: 'Quiet Corner',
    joinedDate: 'April 2023',
  ),

  'DinoLover99': FakeProfileData(
    user: const User(
      id: 'DinoLover99',
      displayName: 'DinoLover99',
      username: 'DinoLover99',
      avatarUrl: 'https://i.pravatar.cc/150?u=dinolover99',
      bio: '🦕 Paleontology enthusiast! Did you know that dinosaurs are '
          'closely related to birds? Ask me anything about dinos! 🦖',
      postCount: 456,
      followerCount: 2340,
      followingCount: 156,
      isPremium: true,
      isVerified: true,
      badges: ['Dino Expert', 'Info Dumper Supreme', 'Community Pillar', 'Verified Nerd', 'Early Adopter'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.explicitExpectations,
    ],
    interests: ['Dinosaurs', 'Fossils', 'Evolution', 'Birds', 'Museums'],
    communicationNotes: 'I LOVE talking about dinosaurs! Feel free to ask '
        'me anything. I might info dump. 🦕',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'I can identify over 300 dinosaur species by their skeletal '
        'structure alone. My apartment has more fossil replicas than furniture 🦴',
    location: 'Natural History Museum',
    joinedDate: 'February 2023',
  ),

  'Alex_Stims': FakeProfileData(
    user: const User(
      id: 'Alex_Stims',
      displayName: 'Alex_Stims',
      username: 'Alex_Stims',
      avatarUrl: 'https://i.pravatar.cc/150?u=alexstims',
      bio: 'Stim toy collector and reviewer. Clicky, squishy, spinny — '
          'I love them all! 🌀✨',
      postCount: 123,
      followerCount: 678,
      followingCount: 234,
      isPremium: false,
      isVerified: false,
      badges: ['Stim Specialist', 'Keyboard Enthusiast', 'Weekly Reviewer'],
    ),
    traits: [
      NeuroDivergentTrait.stimmingPositive,
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.parallelPlay,
      NeuroDivergentTrait.flexibleTiming,
    ],
    interests: ['Stim Toys', 'Mechanical Keyboards', 'ASMR', 'Crafts'],
    communicationNotes: 'Always happy to recommend stim toys! '
        'I review new fidgets weekly. 🌀',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'My mechanical keyboard collection is at 14 and counting. '
        'Each one has a different switch type — I can tell them apart blindfolded!',
    location: 'Fidget Heaven',
    joinedDate: 'July 2023',
  ),

  'SpoonCounter': FakeProfileData(
    user: const User(
      id: 'SpoonCounter',
      displayName: 'SpoonCounter',
      username: 'SpoonCounter',
      avatarUrl: 'https://i.pravatar.cc/150?u=spooncounter',
      bio: 'Chronic illness & neurodivergent. Counting spoons, saving energy, '
          'sharing wisdom. 🥄💜',
      postCount: 67,
      followerCount: 890,
      followingCount: 345,
      isPremium: false,
      isVerified: false,
      badges: ['Spoon Theory Advocate', 'Chronic Warrior'],
    ),
    traits: [
      NeuroDivergentTrait.socialBattery,
      NeuroDivergentTrait.needsProcessingTime,
      NeuroDivergentTrait.textPreferred,
      NeuroDivergentTrait.needsReminders,
    ],
    interests: ['Spoon Theory', 'Chronic Illness Advocacy', 'Cozy Gaming', 'Audiobooks'],
    communicationNotes: 'Response times vary based on my health. '
        'I always reply eventually! 🥄',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.lowBattery,
    funFact: 'I created a spoon-tracking app prototype that went viral in '
        'the chronic illness community. Cozy gaming is my #1 recovery activity 🎮',
    location: 'Cozy Bed',
    joinedDate: 'August 2023',
  ),

  // ── LGBTQ+ community members ──────────────────────────────────────

  'RainbowNerd': FakeProfileData(
    user: const User(
      id: 'RainbowNerd',
      displayName: 'RainbowNerd',
      username: 'RainbowNerd',
      avatarUrl: 'https://i.pravatar.cc/150?u=rainbownerd',
      bio: 'Proudly queer and autistic! Sharing memes, resources, and '
          'celebrating the intersection of LGBTQ+ and neurodivergent identities. 🏳️‍🌈♾️',
      postCount: 312,
      followerCount: 3450,
      followingCount: 678,
      isPremium: true,
      isVerified: true,
      badges: ['Pride Advocate', 'Meme Master', 'Community Builder', 'Intersectionality Champion'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.infoDumpingWelcome,
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.stimmingPositive,
    ],
    interests: ['Queer History', 'Meme Culture', 'Intersectionality', 'Community Building', 'Glitter'],
    communicationNotes: 'Always up for a chat about queer ND experiences! '
        'I use tone indicators and appreciate them back. 🏳️‍🌈',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'I run the largest queer-neurodivergent meme page on the platform. '
        'My glitter stim jar has been featured in 3 TikToks! ✨',
    location: 'Pride Parade (in spirit)',
    joinedDate: 'June 2023',
  ),

  'TransTechie': FakeProfileData(
    user: const User(
      id: 'TransTechie',
      displayName: 'TransTechie',
      username: 'TransTechie',
      avatarUrl: 'https://i.pravatar.cc/150?u=transtechie',
      bio: 'Trans woman in tech. ADHD warrior. Building accessible software '
          'one hyperfocus session at a time. She/Her 🏳️‍⚧️💻',
      postCount: 198,
      followerCount: 2180,
      followingCount: 412,
      isPremium: true,
      isVerified: true,
      badges: ['Tech Pioneer', 'Accessibility Advocate', 'Trans Visibility'],
    ),
    traits: [
      NeuroDivergentTrait.needsProcessingTime,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.flexibleTiming,
      NeuroDivergentTrait.needsReminders,
    ],
    interests: ['Accessible Software', 'Cats', 'Open Source', 'Trans Healthcare Advocacy', 'Mechanical Keyboards'],
    communicationNotes: 'DMs open! I might reply at 3 AM because time blindness. '
        'Please use she/her pronouns. 🏳️‍⚧️',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.hyperfocus,
    funFact: 'I built an open-source screen reader plugin used by 10K+ people. '
        'My 3 cats are named after programming languages: Ruby, Julia, and Perl 🐱',
    location: 'VS Code',
    joinedDate: 'May 2023',
  ),

  'NonBinaryNinja': FakeProfileData(
    user: const User(
      id: 'NonBinaryNinja',
      displayName: 'NonBinaryNinja',
      username: 'NonBinaryNinja',
      avatarUrl: 'https://i.pravatar.cc/150?u=nonbinaryninja',
      bio: 'Enby with AuDHD. Martial arts historian by day, stim toy reviewer '
          'by night. They/Them 💜🥋',
      postCount: 87,
      followerCount: 945,
      followingCount: 267,
      isPremium: false,
      isVerified: false,
      badges: ['Martial Arts Historian', 'AuDHD Pride'],
    ),
    traits: [
      NeuroDivergentTrait.parallelPlay,
      NeuroDivergentTrait.stimmingPositive,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.sensorySensitive,
    ],
    interests: ['Martial Arts History', 'Stim Toys', 'Kata Practice', 'Japanese Culture', 'Fidget Rings'],
    communicationNotes: 'I stim between messages — pauses are normal! '
        'Best communication: short, clear texts. They/them only please. 💜',
    pronouns: 'they/them',
    energyStatus: EnergyStatus.neutral,
    funFact: 'I can perform 12 different traditional kata from memory. '
        'My favorite fidget ring was hand-forged by a blacksmith friend! 🥋',
    location: 'The Dojo',
    joinedDate: 'October 2023',
  ),

  'BiBookworm': FakeProfileData(
    user: const User(
      id: 'BiBookworm',
      displayName: 'BiBookworm',
      username: 'BiBookworm',
      avatarUrl: 'https://i.pravatar.cc/150?u=bibookworm',
      bio: 'Bisexual booklover with dyslexia. Audiobook evangelist. '
          'Reviewing queer YA so you don\'t have to. 💗💜💙📚',
      postCount: 145,
      followerCount: 1670,
      followingCount: 534,
      isPremium: false,
      isVerified: false,
      badges: ['Bookworm', 'Accessibility Advocate', 'Queer Lit Expert'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.textPreferred,
      NeuroDivergentTrait.needsProcessingTime,
      NeuroDivergentTrait.infoDumpingWelcome,
    ],
    interests: ['Queer YA Novels', 'Audiobooks', 'Reading Accessibility', 'Fanfiction', 'Bookbinding'],
    communicationNotes: 'I process text slowly due to dyslexia — please be patient! '
        'Always happy to trade book recs. 📖💗',
    pronouns: 'she/her',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I\'ve listened to 200+ audiobooks this year. My Goodreads challenge '
        'is at 300%. I also hand-bind journals as a stim activity! 📚',
    location: 'Library Corner',
    joinedDate: 'September 2023',
  ),

  'AceExplorer': FakeProfileData(
    user: const User(
      id: 'AceExplorer',
      displayName: 'AceExplorer',
      username: 'AceExplorer',
      avatarUrl: 'https://i.pravatar.cc/150?u=aceexplorer',
      bio: 'Asexual & autistic adventurer! Solo traveler documenting '
          'sensory-friendly destinations worldwide. 🖤🤍💜🌍',
      postCount: 267,
      followerCount: 4120,
      followingCount: 189,
      isPremium: true,
      isVerified: true,
      badges: ['World Traveler', 'Ace Advocate', 'Sensory Guide', 'Top Creator'],
    ),
    traits: [
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.needsQuietSpaces,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.explicitExpectations,
    ],
    interests: ['Solo Travel', 'Sensory-Friendly Tourism', 'Photography', 'Ace Advocacy', 'National Parks'],
    communicationNotes: 'I plan replies like I plan trips — thoroughly. '
        'May take a day but I always respond! 🗺️',
    pronouns: 'he/him',
    energyStatus: EnergyStatus.socialMode,
    funFact: 'I\'ve visited 23 countries solo and rated every single one on a '
        'sensory-friendliness scale. Japan scored highest! 🇯🇵',
    location: 'Somewhere Quiet & Beautiful',
    joinedDate: 'April 2023',
  ),

  'PanPride_Sam': FakeProfileData(
    user: const User(
      id: 'PanPride_Sam',
      displayName: 'Pan Pride Sam',
      username: 'PanPride_Sam',
      avatarUrl: 'https://i.pravatar.cc/150?u=panpridesam',
      bio: 'Pansexual, ADHD, and proud! Creating neurodivergent-affirming '
          'LGBTQ+ art. Commissions always open! 💖💛💙🎨',
      postCount: 234,
      followerCount: 1890,
      followingCount: 623,
      isPremium: false,
      isVerified: false,
      badges: ['Pride Artist', 'ADHD Creative', 'Commission King'],
    ),
    traits: [
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.flexibleTiming,
      NeuroDivergentTrait.stimmingPositive,
      NeuroDivergentTrait.parallelPlay,
    ],
    interests: ['Digital Art', 'Pride Art', 'Neurodivergent Representation', 'Color Theory', 'Commissions'],
    communicationNotes: 'Commission inquiries welcome! I hyperfocus on art so '
        'replies may be delayed. He/they pronouns! 💖',
    pronouns: 'he/they',
    energyStatus: EnergyStatus.fullyCharged,
    funFact: 'My pansexual pride flag reimagined as a neurodiversity infinity symbol '
        'has been shared 15K+ times. Art is my favorite stim! 🎨',
    location: 'Art Studio',
    joinedDate: 'July 2023',
  ),

  'QueerCoder': FakeProfileData(
    user: const User(
      id: 'QueerCoder',
      displayName: 'QueerCoder',
      username: 'QueerCoder',
      avatarUrl: 'https://i.pravatar.cc/150?u=queercoder',
      bio: 'Queer software engineer building inclusive apps. Autistic & proud. '
          'Open source accessibility advocate. 🌈💻♾️',
      postCount: 378,
      followerCount: 5670,
      followingCount: 234,
      isPremium: true,
      isVerified: true,
      badges: ['Open Source Champion', 'Accessibility Engineer', 'Queer in Tech', 'Top Contributor', 'Verified Dev'],
    ),
    traits: [
      NeuroDivergentTrait.directCommunicator,
      NeuroDivergentTrait.passionateInterests,
      NeuroDivergentTrait.routineOriented,
      NeuroDivergentTrait.explicitExpectations,
    ],
    interests: ['Accessibility Engineering', 'Open Source', 'Inclusive Design', 'Rust', 'Code Reviews'],
    communicationNotes: 'I communicate very directly — not rude, just autistic. '
        'PRs and issues preferred over DMs for code stuff. 🌈',
    pronouns: 'they/he',
    energyStatus: EnergyStatus.hyperfocus,
    funFact: 'My accessibility linting library has 2K+ GitHub stars. '
        'I once reviewed 47 PRs in a single hyperfocus session! 💻',
    location: 'GitHub',
    joinedDate: 'March 2023',
  ),

  'LesbianLuna': FakeProfileData(
    user: const User(
      id: 'LesbianLuna',
      displayName: 'LesbianLuna',
      username: 'LesbianLuna',
      avatarUrl: 'https://i.pravatar.cc/150?u=lesbianluna',
      bio: 'Lesbian artist with autism. Creating cozy, sensory-friendly '
          'digital art. Cat mom x3. She/They 🧡🤍💗🐱',
      postCount: 112,
      followerCount: 1230,
      followingCount: 456,
      isPremium: false,
      isVerified: false,
      badges: ['Cozy Creator', 'Cat Mom', 'Sensory Artist'],
    ),
    traits: [
      NeuroDivergentTrait.sensorySensitive,
      NeuroDivergentTrait.smallGroups,
      NeuroDivergentTrait.parallelPlay,
      NeuroDivergentTrait.needsQuietSpaces,
    ],
    interests: ['Digital Art', 'Cats', 'Cozy Games', 'Sensory-Friendly Aesthetics', 'Cottagecore'],
    communicationNotes: "I'm an introvert who creates best in silence. "
        "DMs welcome but replies may come in waves. She/they 🧡",
    pronouns: 'she/they',
    energyStatus: EnergyStatus.recharging,
    funFact: 'My "Cozy Autism" art series has been printed on merch in 5 countries. '
        'All 3 of my cats are named after Studio Ghibli characters! 🐱',
    location: 'Cozy Studio',
    joinedDate: 'November 2023',
  ),
};

/// Look up a fake profile by userId; returns null if no matching fake exists.
FakeProfileData? getFakeProfile(String userId) => _fakeProfiles[userId];

/// All fake-profile user IDs, in the order they were defined.
List<String> get fakeProfileIds => _fakeProfiles.keys.toList();

/// Provider that exposes the full fake profile data (traits, interests, etc.)
final fakeProfileDataProvider = Provider.family<FakeProfileData?, String>((ref, userId) {
  return _fakeProfiles[userId];
});

final profileProvider = FutureProvider.family<User, String>((ref, userId) async {
  await Future.delayed(const Duration(milliseconds: 500));

  // Check the fake-profile database first
  final fakeProfile = _fakeProfiles[userId];
  if (fakeProfile != null) {
    return fakeProfile.user;
  }

  // Fallback for unknown user IDs
  return User(
    id: userId,
    displayName: 'User $userId',
    username: 'user_$userId',
    avatarUrl: 'https://i.pravatar.cc/150?img=$userId',
    bio: 'Hello! I\'m a NeuroComet user.',
    postCount: 15,
    followerCount: 234,
    followingCount: 89,
    isFollowing: false,
  );
});

final userPostsProvider = FutureProvider.family<List<Post>, String>((ref, userId) async {
  await Future.delayed(const Duration(milliseconds: 300));

  // Check if we have a fake profile for personality-consistent posts
  final fakeProfile = _fakeProfiles[userId];
  if (fakeProfile != null) {
    return _generatePersonalityPosts(fakeProfile);
  }

  return List.generate(
    10,
    (index) => Post(
      id: 'post_${userId}_$index',
      authorId: userId,
      authorName: 'User $userId',
      authorAvatarUrl: 'https://i.pravatar.cc/150?img=$userId',
      content: 'This is post #${index + 1} from User $userId! 🎉',
      likeCount: (index * 7) % 100,
      commentCount: (index * 3) % 50,
      createdAt: DateTime.now().subtract(Duration(days: index)),
    ),
  );
});

/// Generate personality-consistent posts for a fake profile
List<Post> _generatePersonalityPosts(FakeProfileData profile) {
  final user = profile.user;
  final posts = <String>[];

  // Generate content based on their interests and personality
  switch (user.username) {
    case 'HyperFocusCode':
      posts.addAll([
        'Just shipped a new accessibility feature at 2 AM because hyperfocus hit at the worst time. '
            'But hey, users will benefit! 💻 #DevLife #ADHD',
        'My triple-alarm system update: added a 4th alarm that just says "FOOD. EAT. NOW." '
            'because apparently that\'s a thing I need 😅 #ADHDHacks',
        'Hot take: The best code is written during hyperfocus sessions between midnight and 4 AM. '
            'The worst code is written during those same sessions. It\'s a coin flip 🪙',
        'New mechanical keyboard arrived and I\'ve been typing random words for 20 minutes just for the sound. '
            'This is peak ADHD stimming and I\'m here for it ⌨️',
        'Body doubling pro tip: pair a boring task with a friend doing their boring task. '
            'We both did our taxes today! Well, I did 80% before getting distracted... progress! 💪',
      ]);
    case 'SensorySeeker':
      posts.addAll([
        'Weighted blanket tier list update: The 15lb bamboo fabric one is STILL my #1. '
            'Nothing beats that deep pressure input after a long day 💙 #SensoryFriendly',
        'Found a café with dim lighting, soft music, AND no strong food smells. '
            'This is my new home now. I live here. 🏠☕',
        'Sensory win today: wore my noise-canceling earbuds to the grocery store. '
            'Zero meltdowns. Zero overwhelm. Technology is beautiful 🎧',
        'Unpopular opinion: "just get used to it" is the worst advice for sensory issues. '
            'Our nervous systems aren\'t broken — environments are just poorly designed.',
        'New review dropping tomorrow: 5 weighted lap pads under $30. Spoiler: the purple one wins 💜',
      ]);
    case 'NeuroNurse':
      posts.addAll([
        '🧠 Neuroscience fact: The ADHD brain doesn\'t lack dopamine — it has trouble REGULATING it. '
            'That\'s why you can hyperfocus on things you love but struggle with "boring" tasks.',
        'New research: Neurodivergent individuals in supportive communities report 67% better mental health outcomes. '
            'Community is medicine. You being here matters 💜',
        'Friendly reminder: Self-diagnosis is valid. Not everyone has access to (expensive) formal assessments. '
            'Trust your lived experience. 🧠✨',
        'Q: "Why can\'t I just try harder?" A: Because ADHD isn\'t about effort — it\'s about how your brain '
            'allocates attention. You\'re not lazy, you\'re wired differently.',
        'The dopamine system explained with cooking: Neurotypical brains have a thermostat. '
            'ADHD brains have a campfire — amazing when it\'s lit, but you can\'t always control the flame 🔥',
      ]);
    case 'QuietQueen':
      posts.addAll([
        'Desk sensory kit update 2.0: Added a mini succulent, a textured mousepad, and switched to '
            'lavender hand cream. The vibes are immaculate 🌱✨',
        'Had to explain "social battery" to my manager today. Drew an actual battery diagram. '
            'She gets it now and we adjusted my meeting schedule. Advocacy works! 🔋',
        'Quiet Saturday checklist: ✅ Water plants ✅ Journal ✅ One small social interaction (texting counts) '
            '✅ Cozy blanket time. Introvert paradise 🌿',
        'Hot take: Open office plans are sensory nightmares disguised as "collaboration spaces." '
            'Give me a quiet corner and I\'ll be 3x more productive. 🤫',
        'New plant for the collection: a string of pearls! My desk garden now has 12 plants. '
            'They never interrupt me and they always look happy to see me 🌿💚',
      ]);
    case 'FocusFounder':
      posts.addAll([
        'Body doubling session starting in 30 mins! Join me for 2 hours of focused work. '
            'No talking, just vibes and productivity. Link in bio! 💪✨',
        'We just hit 200 daily participants in our focus rooms! This community is incredible. '
            'Your presence helps others focus and theirs helps you. That\'s the magic 🪄',
        'Pomodoro tip for ADHD brains: Don\'t do 25/5. Try 15/5 or even 10/3. '
            'Shorter bursts match our attention patterns better. Customize the technique! ⏱️',
        'Someone told me body doubling "isn\'t real." I pointed them to 200 people in our focus room '
            'all getting stuff done together. Evidence: 1, Skeptic: 0 💪',
        'Today\'s focus room theme: "Tax Season Survival." We\'re all filing taxes together. '
            'Misery loves company but productivity loves body doubling! 📋',
      ]);
    case 'ADHDMemes':
      posts.addAll([
        'Me: I\'ll just check one thing real quick\n\n*3 hours later*\n\nMe: Wait what was I doing? 🤔😂',
        'ADHD be like: Can\'t remember where I put my keys 5 seconds ago but can recall a random conversation '
            'from 2007 in vivid detail 🧠✨',
        'The ADHD experience: Starting 47 projects, finishing 3, and somehow getting a new hobby before lunch 🎨🎸📚🎮',
        '"Just write it down so you don\'t forget!"\n\nMe: *writes it down*\n\nMe: *forgets I wrote it down*\n\n'
            'Me: *finds note 6 months later* "what does this mean??" 📝😭',
        'POV: You\'re explaining something you\'re passionate about and suddenly realize you\'ve been talking for '
            '20 minutes and everyone has that glazed look 👀 #InfoDump',
      ]);
    case 'AutismAdvocate':
      posts.addAll([
        '🧵 Thread: Things I wish people understood about autism masking:\n\n'
            '1. It\'s exhausting\n2. It\'s often unconscious\n'
            '3. "You don\'t look autistic" isn\'t a compliment\n4. We do it to survive, not to deceive',
        'Late diagnosis changed my life. At 32, everything finally made sense. '
            'Every struggle, every "quirk," every time I felt like an alien — there was a reason. '
            'You\'re not too old to discover yourself 💙',
        'Today I unmasked at work for the first time: stimmed during a meeting, asked for written instructions, '
            'and said "I need a break" without apologizing. Terrifying but liberating ✨',
        'Stop telling autistic people to "just be themselves" and then punishing us when we are. '
            'Acceptance means accepting the stims, the routines, and the directness too.',
        'My cat understands me better than most humans. She respects my need for quiet, doesn\'t demand eye contact, '
            'and parallel plays with me perfectly. 10/10 companion 🐱💙',
      ]);
    case 'TherapyTips':
      posts.addAll([
        '📊 Study just released: Neurodivergent individuals who found supportive communities '
            'reported 67% improvement in mental health outcomes.\n\nCommunity matters. You matter. 💜',
        'Therapy tip: The "5-4-3-2-1" grounding technique works differently for ND brains. '
            'Try focusing on just ONE sense deeply instead of cycling through all five. Quality > quantity.',
        'Reminder: Rest is not laziness. Especially for neurodivergent people, recovery time '
            'is a biological necessity. You cannot pour from an empty cup 🫗',
        'New free worksheet dropped: "Understanding Your Sensory Profile" — identify triggers, '
            'find soothing strategies, and communicate needs to others. Link in bio! 📋',
        'The difference between burnout and autistic burnout: one recovers with vacation, '
            'the other may need months of accommodation changes. Know the difference. 🧠',
      ]);
    case 'NeuroNaut':
      posts.addAll([
        'Exploring the intersection of autism and creativity today. Did you know many famous '
            'inventors were likely neurodivergent? 🧠🌈',
        'Info dump incoming: fiber arts as a regulation stim. Knitting activates bilateral '
            'coordination and provides rhythmic sensory input. Basically therapy with yarn! 🧶',
        'Reminder: being autistic doesn\'t mean we lack empathy. We often feel TOO much. '
            'The difference is in how we express it. 💜',
        'Started watching Star Trek again from the beginning. Third rewatch. Each time I catch '
            'new details — the autistic attention to detail is a feature, not a bug! 🖖',
        'Community highlight: This space has grown so much. Seeing neurodivergent people '
            'support each other openly is exactly why I started posting here. 🧠🌈',
      ]);
    case 'CalmObserver':
      posts.addAll([
        'Today\'s low-stimulation tip: try "soft fascination" — watching clouds, flowing water, '
            'or leaves rustling. It restores attention without overwhelming the senses 🍃',
        'I check messages twice daily and that\'s okay. Boundaries aren\'t rude — they\'re survival. 🧘',
        'Made the perfect cup of tea today. Sometimes the smallest rituals bring the deepest peace ☕✨',
        '30 days of screen-free mornings: the difference in my sensory regulation has been remarkable. '
            'Journaling about it in my analog notebook 📓',
        'Nature photography session today. No rush, no agenda. Just observing light through leaves. '
            'This is what mindfulness looks like for my brain 📸🌿',
      ]);
    case 'DinoLover99':
      posts.addAll([
        '🦕 DID YOU KNOW: T-Rex had the most powerful bite force of any land animal EVER — '
            'roughly 12,800 pounds! That\'s enough to crush bone. Incredible! 🦖',
        'Museum trip today! Found a beautifully preserved Triceratops horn core. '
            'The detail in the fossilization process is just *chef\'s kiss* 🦴✨',
        'Hot take: Birds ARE dinosaurs. Not "descended from" — they literally ARE theropod '
            'dinosaurs. Every chicken is a tiny dinosaur. You\'re welcome. 🐔🦖',
        'Someone asked me to "briefly" explain the Cretaceous period. Two hours later they '
            'know about impact winters, feathered raptors, and marine reptiles. You asked. 🦕',
        'New fossil replica arrived for my collection: a Velociraptor claw. It\'s smaller '
            'than people think (thanks, Jurassic Park). But PERFECTLY formed. 🦴',
      ]);
    case 'Alex_Stims':
      posts.addAll([
        'New stim toy review: This infinity cube has the most satisfying click I\'ve ever heard. '
            '10/10 would recommend for meetings 🌀',
        'Mechanical keyboard update: Cherry MX Brown switches are my current favorite. The tactile bump '
            'without the loud click is *perfect* for stimming while coding ⌨️✨',
        'Built a custom fidget board today! Velcro strips, buttons, switches, and a tiny marble maze. '
            'Total cost: \$8. Satisfaction: priceless 🎮',
        'Stim toy tier list update: Magnetic putty moved up to S-tier. The way it slowly swallows '
            'a magnet is endlessly satisfying. I could watch it for hours 🧲',
        'Weekly fidget recommendation: These silicone bubble pop keychains are so underrated. '
            'Quiet enough for libraries, satisfying enough for regulation. 💯',
      ]);
    case 'SpoonCounter':
      posts.addAll([
        'Spoon check! Started with 10 today, used 4 on groceries and 2 on a phone call. '
            'Saving the rest for evening gaming 🥄🎮',
        'Reminder: You don\'t have to earn rest. Rest IS productive. '
            'Your body is doing incredible work just existing. 🥄💜',
        'Audiobook recommendation for low-spoon days: anything narrated by someone with a '
            'soothing voice. Let your brain be gently entertained 📚',
        'Bad spoon day today. Made it from bed to couch. That counts. '
            'Tomorrow might be better. Tonight I have cozy games 🎮💜',
        'Pro tip for fellow spoonies: Meal prep on high-energy days. '
            'Past-me made soup for future-me and future-me is SO grateful 🍲🥄',
      ]);
    case 'RainbowNerd':
      posts.addAll([
        'The overlap between queer and neurodivergent communities is HUGE and we need to talk '
            'about it more. Research shows higher rates of autism in LGBTQ+ populations. 🏳️‍🌈♾️',
        'New meme dropped: "Is this a special interest or a hyperfixation?" — it can be both! '
            'Especially when it\'s queer history 😂🌈',
        'Happy pride from your favorite queer autistic meme lord! Remember: you don\'t need to '
            'mask ANY part of yourself here. 🏳️‍🌈💜',
        'Info dump: the history of LGBTQ+ neurodivergent activists is SO rich and underrepresented. '
            'Thread coming this week! 🧵🌈',
        'Made a glitter stim jar in pride flag colors. Watching the glitter settle is my new '
            'favorite regulation tool. Form AND function! ✨🏳️‍🌈',
      ]);
    case 'TransTechie':
      posts.addAll([
        'Shipped an accessibility feature at 3 AM because ADHD said "now or never." '
            'My cats judged me but the users will benefit 🏳️‍⚧️💻',
        'Being trans AND having ADHD means I forget to take my meds at the same time every day. '
            'Pill organizer + 3 phone alarms = survival kit 💊⏰',
        'Ruby (the cat, not the language) just walked across my keyboard and somehow fixed a bug. '
            'Hiring her immediately 🐱💻',
        'Open sourced my screen reader plugin today! 10K+ users and growing. '
            'Accessible tech is a right, not a privilege 🏳️‍⚧️♿',
        'Trans joy: Getting correctly gendered by a stranger for the first time today while '
            'hyperfocusing on code at a café. Best. Day. Ever. 🏳️‍⚧️💜',
      ]);
    case 'NonBinaryNinja':
      posts.addAll([
        'Kata practice is the ultimate stim. Repetitive movement, deep focus, full body '
            'engagement. Martial arts saved my regulation 🥋💜',
        'My new fidget ring arrived — hand-forged steel with a spinning outer band. '
            'Using it between kata sets and it\'s perfect ⚔️🌀',
        'Friendly reminder: they/them pronouns aren\'t hard. You use them for strangers '
            'every day. Practice makes perfect 💜',
        'AuDHD experience: Hyperfocusing on martial arts history for 6 hours, then forgetting '
            'to eat. The duality of having both is... something 🥋😅',
        'Morning routine: 20 minutes of kata, sensory-friendly breakfast, fidget ring during '
            'commute. Structure is freedom for my brain 📅',
      ]);
    case 'BiBookworm':
      posts.addAll([
        'Just finished the most incredible queer YA audiobook. Bisexual protagonist with '
            'ADHD navigating college. I felt SO seen. 💗💜💙📚',
        'Dyslexia hack: audiobooks at 1.25x speed. Fast enough to keep ADHD brain engaged, '
            'slow enough for processing. Game changer! 🎧',
        'Hand-bound a new journal today as a stim activity. The repetitive motion of sewing '
            'signatures is incredibly soothing 📖✨',
        'Reading accessibility matters! Not everyone processes text the same way. Audiobooks, '
            'dyslexic fonts, read-aloud tools — they\'re all valid ways to read 📚',
        'Currently have 14 audiobooks in progress simultaneously. ADHD reading is chaotic but '
            'I wouldn\'t trade it for anything 🎧💗💜💙',
      ]);
    case 'AceExplorer':
      posts.addAll([
        'Sensory-friendly travel tip: Always pack noise-canceling headphones, a familiar comfort '
            'item, and a written itinerary. Predictability = less overwhelm 🗺️🎧',
        'Visited a national park today and rated it 9/10 on sensory friendliness. '
            'Quiet trails, no crowds, birdsong only. Paradise. 🌲🖤🤍💜',
        'Solo travel as an autistic person: people think I\'m brave. Really I just prefer my own '
            'schedule and no surprise social plans 😅🌍',
        'Country #23: Iceland! Sensory rating: 10/10. Wide open spaces, minimal crowds, and '
            'the Northern Lights are the best stim I\'ve ever experienced 🇮🇸✨',
        'Ace pride + autism: both are about people not understanding what you DON\'T feel. '
            'We\'re complete as we are. 🖤🤍💜♾️',
      ]);
    case 'PanPride_Sam':
      posts.addAll([
        'New art piece: the pansexual flag reimagined as a neurodiversity infinity symbol. '
            'This is what hyperfocus at 2 AM creates 💖💛💙🎨',
        'Commissions are open! Specializing in neurodivergent-affirming LGBTQ+ portraits. '
            'Art is my favorite stim 🎨✨',
        'Color theory fun fact: the pan flag colors (pink, yellow, blue) create a perfect '
            'complementary triad. No wonder it looks so good! 💖💛💙',
        'ADHD art process: Sketch → get distracted → come back → realize the "mistake" is '
            'actually the best part → finish at 4 AM → sleep for 12 hours 🎨😅',
        'New prints available! The neurodiversity + pride series is my most personal work yet. '
            'Every piece is a celebration of who we are 💖💛💙♾️',
      ]);
    case 'QueerCoder':
      posts.addAll([
        'Merged my 100th accessibility PR today. Every alt text, every ARIA label, every keyboard '
            'shortcut matters. Inclusive code = better code 🌈💻',
        'Autistic communication style: "This code has a bug" means exactly that. Not '
            '"you\'re a bad developer." Direct ≠ rude. 🏳️‍🌈',
        'My Rust accessibility linter just hit 2K stars on GitHub. Proof that hyperfocus + '
            'special interest = impact ⭐♾️',
        'Code review tip: When I say "this needs changes," I mean exactly that — no hidden '
            'subtext, no passive aggression. Autistic directness is an asset in tech 💻',
        'Open source milestone: Our accessibility testing framework is now used by 500+ companies. '
            'Every PR makes the web more inclusive 🌈♿',
      ]);
    case 'LesbianLuna':
      posts.addAll([
        'New "Cozy Autism" art piece: a girl stimming happily with her cats by a rain-streaked '
            'window. Prints available soon 🧡🤍💗🐱',
        'Studio vibes today: lo-fi music, three cats sleeping nearby, tablet in hand. '
            'This is what recharging looks like for me 🎨💤',
        'My cats are named Totoro, Calcifer, and Kiki. Yes I\'m a Ghibli fan. '
            'Yes they all have tiny pride bandanas 🐱🌈',
        'Creating sensory-friendly art means: soft color palettes, no harsh contrasts, '
            'and textures that feel calming to look at. Art should be a safe space 🎨',
        'Cottagecore + autism is the perfect combo. Give me a quiet garden, my cats, and my '
            'tablet. That\'s the whole dream 🌿🧡🤍💗',
      ]);
    default:
      posts.addAll([
        'Sharing my neurodivergent journey one day at a time! 🧠✨',
        'Found a great new strategy today — will share details soon!',
        'Community check-in: How is everyone doing today? 💜',
      ]);
  }

  return posts.asMap().entries.map((entry) {
    final index = entry.key;
    final content = entry.value;
    return Post(
      id: 'post_${user.id}_$index',
      authorId: user.id,
      authorName: user.displayName,
      authorAvatarUrl: user.avatarUrl,
      content: content,
      likeCount: [47, 283, 156, 92, 534, 1200, 67, 345][index % 8],
      commentCount: [12, 45, 23, 8, 67, 134, 5, 29][index % 8],
      createdAt: DateTime.now().subtract(Duration(hours: index * 8 + 2)),
    );
  }).toList();
}

