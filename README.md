# NeuroNet 🧠

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Platform: Android"/>
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-purple?style=for-the-badge&logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue?style=for-the-badge&logo=jetpackcompose" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26-orange?style=for-the-badge" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Version-1.0.0--beta-yellow?style=for-the-badge" alt="Version"/>
</p>

<p align="center">
  <b>A social platform designed with neurodivergent users in mind.</b><br/>
  Built with accessibility, safety, and mental wellness as core priorities.
</p>

---

## ✨ Features

### 🎯 Core Social Features
- **Feed** - Browse and interact with posts from the community
- **Stories** - Share ephemeral content with customizable durations
- **Direct Messages** - Private conversations with safety features built-in
- **Explore** - Discover new content and categories
- **Notifications** - Stay updated with customizable alerts

### 🧠 Neuro-Centric Design
- **Neuro-State Themes** - Adaptive UI themes based on mental states:
  - **Default** - Balanced, neutral appearance
  - **Hyperfocus** - High contrast, minimal distractions
  - **Overload** - Calming colors, reduced visual noise
  - **Calm** - Soft, soothing color palette
- **Adjustable Text Sizes** - Small, Medium, Large, X-Large options
- **High Contrast Mode** - Pure black/white for visual accessibility
- **Dark Mode** - Reduce eye strain with full dark theme support

### 👨‍👩‍👧‍👦 Safety & Parental Controls
- **Age Verification** - Protect younger users with age-appropriate restrictions
- **Kids Mode** - Disable DMs and restrict content for under-13 users
- **Content Filtering** - AI-powered moderation for safe interactions
- **Block & Mute** - User controls for managing interactions
- **Report System** - Flag inappropriate content or behavior

### 🏆 Gamification & Engagement
- **Badge System** - Earn achievements for positive engagement:
  - 🔵 Verified Human
  - ✍️ First Post
  - 🎯 HyperFocus Master
  - 🏛️ Community Pillar
  - 🤫 Quiet Achiever
- **Progress Tracking** - View earned and locked badges

### 📞 Communication
- **WebRTC Voice & Video Calls** - Real-time communication with peers
- **Real-time Messaging** - Instant message delivery via Supabase Realtime
- **Typing Indicators** - See when others are composing messages

### 💎 Premium Features
- **Ad-Free Experience** - Remove all advertisements
- **Monthly Subscription** - $2/month
- **Lifetime Access** - $60 one-time purchase

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin 2.1.0 |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Architecture** | MVVM with ViewModels |
| **Backend** | Supabase (PostgreSQL, Auth, Realtime) |
| **Authentication** | Firebase Auth + Supabase Auth |
| **Payments** | RevenueCat |
| **Video/Voice** | Stream WebRTC Android |
| **Media Playback** | Media3 ExoPlayer |
| **Image Loading** | Coil |
| **Networking** | Ktor Client |
| **Serialization** | Kotlinx Serialization |

---

## 📋 Requirements

- **Android Studio** - Latest stable version (Ladybug or newer)
- **JDK** - Java 17
- **Android SDK** - API 26+ (Android 8.0 Oreo minimum)
- **Gradle** - 9.1.0
- **Target SDK** - 36

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/NeuroNet.git
cd NeuroNet
```

### 2. Configure local.properties

Create or edit `local.properties` in the project root:

```properties
# Android SDK
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# Supabase Configuration (Required)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your_anon_public_key

# Developer Options (Optional)
DEVELOPER_DEVICE_HASH=your_device_hash
```

### 3. Set Up Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing
3. Add Android app with package: `com.kyilmaz.neuronetworkingtitle`
4. Download `google-services.json` and place in `app/` directory

### 4. Set Up Supabase

1. Create a project at [Supabase](https://supabase.com/)
2. Run the database schema from `SETUP_GUIDE.md`
3. Configure Row Level Security (RLS) policies
4. Copy API credentials to `local.properties`

### 5. Build and Run

```bash
./gradlew assembleDebug
```

Or use Android Studio: **Run > Run 'app'**

---

## 📁 Project Structure

```
app/src/main/
├── java/com/kyilmaz/neuronetworkingtitle/
│   ├── MainActivity.kt           # App entry point
│   ├── NeuroNetApplication.kt    # Application class
│   │
│   ├── # Authentication
│   ├── AuthScreen.kt             # Login/Register UI
│   ├── AuthViewModel.kt          # Auth state management
│   │
│   ├── # Feed & Posts
│   ├── FeedScreen.kt             # Main feed UI
│   ├── FeedViewModel.kt          # Feed data management
│   ├── Post.kt                   # Post data model
│   ├── PostCard.kt               # Post UI component
│   │
│   ├── # Stories
│   ├── Story.kt                  # Story data model
│   ├── StoryScreen.kt            # Story viewer
│   ├── StoryViewer.kt            # Story playback
│   │
│   ├── # Messaging
│   ├── DmScreens.kt              # Direct messages UI
│   ├── ChatViewModel.kt          # Chat state management
│   ├── calling/                  # WebRTC voice/video calls
│   │
│   ├── # Settings & Themes
│   ├── SettingsScreen.kt         # Settings UI
│   ├── ThemeSettings.kt          # Theme configuration
│   ├── Theming.kt                # Theme definitions
│   ├── NeuroState.kt             # Neuro-centric states
│   │
│   ├── # Safety
│   ├── ParentalControls.kt       # Parental control logic
│   ├── ContentFiltering.kt       # Content moderation
│   ├── ModerationService.kt      # Moderation API
│   │
│   ├── # Premium
│   ├── SubscriptionManager.kt    # RevenueCat integration
│   ├── SubscriptionScreen.kt     # Premium purchase UI
│   │
│   └── # Backend
│       └── SupabaseClient.kt     # Supabase configuration
│
└── res/
    ├── values/
    │   └── strings.xml           # Localized strings
    └── ...
```

---

## 🔧 Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `SUPABASE_URL` | Supabase project URL | ✅ Yes |
| `SUPABASE_KEY` | Supabase anon/public key | ✅ Yes |
| `DEVELOPER_DEVICE_HASH` | SHA256 hash for dev options | ❌ No |

### RevenueCat Products

| Product ID | Type | Price |
|------------|------|-------|
| `neuronet_premium_monthly` | Subscription | $2.00/month |
| `neuronet_premium_lifetime` | In-app product | $60.00 |

---

## 🧪 Developer Options

Developer options are available for testing and debugging. To enable:

1. Run the app in debug mode
2. Navigate to Settings > Developer Options
3. Check Logcat for `DEV_ACCESS` tag to get your device hash
4. Add `DEVELOPER_DEVICE_HASH=<hash>` to `local.properties`

### Available Dev Options
- Force verify user status
- Simulate premium subscription
- Mock interface with fake data
- Simulate badge notifications
- Database management tools

---

## 📱 Screenshots

*Coming soon*

---

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is proprietary software. All rights reserved.

---

## 📞 Support

For support, please open an issue on GitHub or contact the development team.

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Supabase](https://supabase.com/) - Open source Firebase alternative
- [Stream WebRTC](https://getstream.io/) - Real-time communication
- [RevenueCat](https://www.revenuecat.com/) - In-app subscription management
- The neurodivergent community for inspiration and feedback

---

<p align="center">
  Made with ❤️ for the neurodivergent community
</p>

