# NeuroComet App - Complete Setup Guide
## From Start to Finish: Configuration Before Testing

---

## 📋 Prerequisites Checklist

Before you begin, ensure you have:
- [ ] Android Studio installed (latest stable version)
- [ ] Android SDK 26+ (API Level 26 minimum)
- [ ] A physical Android device or emulator
- [ ] Internet connection for backend services

---

## 🚀 Step 1: Clone/Open the Project

```bash
# If cloning from git
git clone <your-repo-url>
cd NeuroComet

# Open in Android Studio
# File → Open → Select the project folder
```

---

## 🔐 Step 2: Create `local.properties`

**Location**: `C:\Users\bkyil\AndroidStudioProjects\NeuroComet\local.properties`

Add these lines to the file:

```properties
# Android SDK (already configured)
sdk.dir=C\:\\Users\\bkyil\\AppData\\Local\\Android\\Sdk

# ============================================
# REVENUECAT CONFIGURATION (In-App Purchases)
# ============================================
REVENUECAT_API_KEY=goog_tqbgqglcFYAbwdfvHDhNLgZtXWb

# ============================================
# SUPABASE CONFIGURATION (Required for backend)
# ============================================
# Get these from: https://supabase.com/dashboard → Your Project → Settings → API
SUPABASE_URL=https://cdaeimusmufwfixdpoep.supabase.co
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNkYWVpbXVzbXVmd2ZpeGRwb2VwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU5MjIyMTEsImV4cCI6MjA4MTQ5ODIxMX0.56u9RpNMNi1hu1ntAoGFwuq_Q-tXDHJw7RHM2p1yNCU

# ============================================
# DEVELOPER OPTIONS (Optional)
# ============================================
# To enable dev options on your device, first run the app,
# try to access dev options, then check Logcat for "DEV_ACCESS"
# to find your device hash
DEVELOPER_DEVICE_HASH=your_sha256_hash_here
```

### How to get Supabase credentials:
1. Go to https://supabase.com/dashboard
2. Select your project (or create one)
3. Click **Settings** (gear icon) → **API**
4. Copy:
   - **Project URL** → paste as `SUPABASE_URL`
   - **anon public** key → paste as `SUPABASE_KEY`

> Note: These `local.properties` values are used by the native Android app under `app/`.
> The Flutter app under `flutter_app/` does **not** read the root `local.properties` file for Supabase.
> Start the Flutter app with compile-time defines instead:
>
> `--dart-define=SUPABASE_URL=https://YOUR-PROJECT-REF.supabase.co`
>
> `--dart-define=SUPABASE_ANON_KEY=eyJ...`

---

## 🔥 Step 3: Configure Firebase (google-services.json)

**Location**: `app/google-services.json`

**Current Status**: Contains placeholder values that need to be replaced.

### Steps:
1. Go to https://console.firebase.google.com/
2. Create a new project or select existing one
3. Click **Add app** → **Android**
4. Use the actual native Android package names from `app/build.gradle.kts`:
   - Release: `com.kyilmaz.neurocomet`
   - Debug: `com.kyilmaz.neurocomet.debug`
5. Download `google-services.json`
6. Place it at `app/google-services.json` if the file contains the client you need, or use variant-specific files such as `app/src/debug/google-services.json` for the debug package.

### Services to enable in Firebase Console:
- [ ] **Authentication** (if using Firebase Auth)
- [ ] **Cloud Messaging** (for push notifications)
- [ ] **Analytics** (optional, for usage tracking)

---

## 💰 Step 4: Configure RevenueCat (In-App Purchases)

NeuroComet offers two subscription options:
- **Monthly**: $2/month ad-free subscription
- **Lifetime**: $60 one-time purchase for permanent ad-free access

### 4.1 RevenueCat Dashboard Setup

1. Go to https://app.revenuecat.com/
2. Create an account and project
3. Add your app (Android - Google Play)
4. Go to **API Keys** → Copy your **Public SDK Key**

### 4.2 Configure Products in Google Play Console

1. Go to Google Play Console → Your App → **Monetization** → **Products**
2. Create these products:

| Product ID | Type | Price | Description |
|------------|------|-------|-------------|
| `neurocomet_premium_monthly` | Subscription | $2.00/month | Monthly ad-free subscription |
| `neurocomet-premium-lifetime` | In-app product | $60.00 | Lifetime ad-free access |

### 4.3 Configure RevenueCat Products

1. In RevenueCat dashboard, go to **Products**
2. Add both products with the same IDs
3. Create an **Offering** called "default" containing:
   - Monthly package: `neurocomet_premium_monthly`
   - Lifetime package: `neurocomet-premium-lifetime`
4. Create an **Entitlement** called `entlaba9aee067`
5. Attach both products to the `entlaba9aee067` entitlement

### 4.4 Update API Key in Code

**Current Status**: The app automatically reads the key from `local.properties`.

### 4.5 Testing Subscriptions

For testing without real purchases:
1. Go to **Settings** → **Go Premium** in the app
2. Use Google Play test accounts for sandbox purchases
3. Or enable "Fake Premium" in DevOptions for UI testing

### For testing without RevenueCat:
The app will still work, but premium features won't be purchasable.
You can manually test premium features in DevOptions.

---

## 🗄️ Step 5: Set Up Supabase Database

### 5.1 Create Required Tables

Go to Supabase Dashboard → **SQL Editor** and run each block separately:

#### Block 1: Enable UUID Extension
```sql
-- Enable UUID extension (required for gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

#### Block 2: Users Table
```sql
-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    username TEXT UNIQUE,
    display_name TEXT,
    avatar_url TEXT,
    bio TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
```

#### Block 3: Posts Table
```sql
-- Posts table
CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT,  -- Using TEXT for flexibility with mock user IDs
    content TEXT NOT NULL,
    image_url TEXT,
    video_url TEXT,
    likes INTEGER DEFAULT 0,
    comments INTEGER DEFAULT 0,
    shares INTEGER DEFAULT 0,
    category TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create index for user posts lookup
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
```

#### Block 4: Post Likes Table
```sql
-- Post likes (tracks which users liked which posts)
-- Note: No foreign key to posts table - allows likes on mock posts
CREATE TABLE IF NOT EXISTS post_likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id BIGINT NOT NULL,  -- No FK constraint - works with mock posts
    user_id TEXT NOT NULL,    -- Using TEXT for flexibility with mock user IDs
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(post_id, user_id)  -- One like per user per post
);

-- Indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_post_likes_post ON post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_user ON post_likes(user_id);
```

#### Block 5: Conversations & Messages
```sql
-- Conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Conversation participants (many-to-many)
CREATE TABLE IF NOT EXISTS conversation_participants (
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (conversation_id, user_id)
);

-- Direct messages table
CREATE TABLE IF NOT EXISTS dm_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    type TEXT DEFAULT 'text',
    media_url TEXT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for message queries
CREATE INDEX IF NOT EXISTS idx_dm_messages_conversation ON dm_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_dm_messages_created ON dm_messages(created_at DESC);
```

#### Block 5: Message Reactions Table
```sql
-- Message reactions (like WhatsApp/iMessage/Telegram)
CREATE TABLE IF NOT EXISTS message_reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID REFERENCES dm_messages(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    emoji TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(message_id, user_id, emoji) -- One reaction type per user per message
);

-- Indexes for reaction queries
CREATE INDEX IF NOT EXISTS idx_reactions_message ON message_reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_reactions_user ON message_reactions(user_id);
```

#### Block 6: Stories Table
```sql
-- Stories table
CREATE TABLE IF NOT EXISTS stories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    image_url TEXT,
    video_url TEXT,
    duration INTEGER DEFAULT 5000,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '24 hours')
);

-- Index for active stories
CREATE INDEX IF NOT EXISTS idx_stories_user_id ON stories(user_id);
CREATE INDEX IF NOT EXISTS idx_stories_expires_at ON stories(expires_at);
```

### 5.2 Add Missing Columns to Existing Tables

The app code references some columns not in the base schema. Run these to add them:

```sql
-- Users table: add columns for account management and profile banner
ALTER TABLE users ADD COLUMN IF NOT EXISTS banner_url TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deletion_scheduled_at TIMESTAMPTZ;

-- Direct messages table: add columns for message types, media, and read state
ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS type TEXT DEFAULT 'text';
ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS media_url TEXT;
ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT false;

-- Posts table: add columns if upgrading from an older schema
ALTER TABLE posts ADD COLUMN IF NOT EXISTS comments INTEGER DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS shares INTEGER DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS category TEXT;
```

### 5.3 Create Additional Required Tables

The app uses these tables for social features. Run each block separately:

#### Block 7: Follows Table
```sql
CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id);
```

#### Block 8: Blocked Users Table
```sql
CREATE TABLE IF NOT EXISTS blocked_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);

CREATE INDEX IF NOT EXISTS idx_blocked_blocker ON blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_blocked ON blocked_users(blocked_id);
```

#### Block 9: Muted Users Table
```sql
CREATE TABLE IF NOT EXISTS muted_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    muter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    muted_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(muter_id, muted_id)
);

CREATE INDEX IF NOT EXISTS idx_muted_muter ON muted_users(muter_id);
```

#### Block 10: Bookmarks Table
```sql
CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_post ON bookmarks(post_id);
```

#### Block 11: Notifications Table
```sql
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type TEXT NOT NULL,           -- 'like', 'comment', 'follow', 'mention', etc.
    message TEXT,
    target_id TEXT,               -- ID of the post/comment/etc. that triggered it
    target_type TEXT,             -- 'post', 'comment', 'story', etc.
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications(created_at DESC);
```

#### Block 12: Reports Table
```sql
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type TEXT NOT NULL,   -- 'post', 'comment', 'user', 'message'
    content_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    additional_info TEXT,
    status TEXT DEFAULT 'pending', -- 'pending', 'reviewed', 'resolved', 'dismissed'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_reporter ON reports(reporter_id);
```

#### Block 13: Post Comments Table
```sql
CREATE TABLE IF NOT EXISTS post_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id BIGINT NOT NULL,
    user_id TEXT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id UUID,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_post ON post_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user ON post_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON post_comments(parent_comment_id);
```

---

## 📱 Step 6: Build & Run the App

### 6.1 Sync Gradle
In Android Studio:
- Click **File** → **Sync Project with Gradle Files**
- Or click the elephant icon with refresh arrow

### 6.2 Build the Project
```bash
# From terminal in project root
.\gradlew assembleDebug
```

Or in Android Studio:
- **Build** → **Make Project** (Ctrl+F9)

### 6.3 Run on Device/Emulator
- Select your device from the dropdown
- Click **Run** (green play button) or press Shift+F10

---

## ✅ Step 7: Verify Everything Works

### 7.1 Check Logcat for Connection Status
Filter Logcat by: `AppSupabaseClient`

**Success messages**:
```
I/AppSupabaseClient: Initializing Supabase client
```

**Not configured (uses mock data)**:
```
I/AppSupabaseClient: Supabase not configured - using mock data mode
```

---

## 🔧 Quick Reference: All Configuration Files

| File | Purpose | Required? |
|------|---------|-----------|
| `local.properties` | Supabase keys, RC keys, SDK path | Yes |
| `app/google-services.json` | Firebase config | Yes for push notifications |
| `MainActivity.kt` | App entry point | Auto-configured |
| `SupabaseClient.kt` | Backend connection | Auto-configured |

---

**You're ready to test!** 🎉
