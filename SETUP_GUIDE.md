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
# SUPABASE CONFIGURATION (Required for backend)
# ============================================
# Get these from: https://supabase.com/dashboard → Your Project → Settings → API
SUPABASE_URL=https://YOUR-PROJECT-REF.supabase.co
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (your anon/public key)

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
| `NeuroComet_premium_monthly` | Subscription | $2.00/month | Monthly ad-free subscription |
| `NeuroComet_premium_lifetime` | In-app product | $60.00 | Lifetime ad-free access |

### 4.3 Configure RevenueCat Products

1. In RevenueCat dashboard, go to **Products**
2. Add both products with the same IDs
3. Create an **Offering** called "default" containing:
   - Monthly package: `NeuroComet_premium_monthly`
   - Lifetime package: `NeuroComet_premium_lifetime`
4. Create an **Entitlement** called `premium`
5. Attach both products to the `premium` entitlement

### 4.4 Update API Key in Code

**Location**: `app/src/main/java/.../MainActivity.kt` (line ~864)

```kotlin
Purchases.configure(PurchasesConfiguration.Builder(this, "goog_YOUR_API_KEY_HERE").build())
```

Replace `goog_YOUR_API_KEY_HERE` with your actual RevenueCat public API key.

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

### 5.4 Enable RLS on All Tables

```sql
-- Enable RLS on original tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE post_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE dm_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE stories ENABLE ROW LEVEL SECURITY;

-- Enable RLS on new tables
ALTER TABLE follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE muted_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE post_comments ENABLE ROW LEVEL SECURITY;
```

### 5.5 Create RLS Policies

**IMPORTANT:** First, drop any existing policies to avoid conflicts. Run this block first:

```sql
-- Drop all existing policies (run this first to start fresh)
DROP POLICY IF EXISTS "Public profiles are viewable by everyone" ON users;
DROP POLICY IF EXISTS "Users can update own profile" ON users;
DROP POLICY IF EXISTS "Posts are viewable by everyone" ON posts;
DROP POLICY IF EXISTS "Authenticated users can create posts" ON posts;
DROP POLICY IF EXISTS "Users can update own posts" ON posts;
DROP POLICY IF EXISTS "Users can delete own posts" ON posts;
DROP POLICY IF EXISTS "Anyone can view likes" ON post_likes;
DROP POLICY IF EXISTS "Anyone can like posts" ON post_likes;
DROP POLICY IF EXISTS "Users can unlike posts" ON post_likes;
DROP POLICY IF EXISTS "Users can view own conversations" ON conversations;
DROP POLICY IF EXISTS "Users can view conversation participants" ON conversation_participants;
DROP POLICY IF EXISTS "Users can read own messages" ON dm_messages;
DROP POLICY IF EXISTS "Users can send messages" ON dm_messages;
DROP POLICY IF EXISTS "Users can view reactions" ON message_reactions;
DROP POLICY IF EXISTS "Users can add reactions" ON message_reactions;
DROP POLICY IF EXISTS "Users can remove own reactions" ON message_reactions;
DROP POLICY IF EXISTS "Active stories are viewable" ON stories;
DROP POLICY IF EXISTS "Users can create own stories" ON stories;
DROP POLICY IF EXISTS "Users can delete own stories" ON stories;

-- New table policies
DROP POLICY IF EXISTS "Anyone can view follows" ON follows;
DROP POLICY IF EXISTS "Authenticated users can follow" ON follows;
DROP POLICY IF EXISTS "Users can unfollow" ON follows;
DROP POLICY IF EXISTS "Users can view own blocks" ON blocked_users;
DROP POLICY IF EXISTS "Users can block" ON blocked_users;
DROP POLICY IF EXISTS "Users can unblock" ON blocked_users;
DROP POLICY IF EXISTS "Users can view own mutes" ON muted_users;
DROP POLICY IF EXISTS "Users can mute" ON muted_users;
DROP POLICY IF EXISTS "Users can unmute" ON muted_users;
DROP POLICY IF EXISTS "Users can view own bookmarks" ON bookmarks;
DROP POLICY IF EXISTS "Users can create bookmarks" ON bookmarks;
DROP POLICY IF EXISTS "Users can remove bookmarks" ON bookmarks;
DROP POLICY IF EXISTS "Users can view own notifications" ON notifications;
DROP POLICY IF EXISTS "Anyone can create notifications" ON notifications;
DROP POLICY IF EXISTS "Users can update own notifications" ON notifications;
DROP POLICY IF EXISTS "Anyone can create reports" ON reports;
DROP POLICY IF EXISTS "Users can view own reports" ON reports;
DROP POLICY IF EXISTS "Anyone can view comments" ON post_comments;
DROP POLICY IF EXISTS "Anyone can create comments" ON post_comments;
DROP POLICY IF EXISTS "Users can delete own comments" ON post_comments;
```

**Now create the policies (run each one separately):**

**Note:** These policies are permissive for development. For production, you should restrict INSERT/UPDATE/DELETE to authenticated users only.

```sql
-- USERS: Anyone can read user profiles
CREATE POLICY "Public profiles are viewable by everyone" 
ON users FOR SELECT 
USING (true);
```

```sql
-- USERS: Anyone can create users (for testing/development)
CREATE POLICY "Anyone can create users" 
ON users FOR INSERT 
WITH CHECK (true);
```

```sql
-- USERS: Users can update their own profile
CREATE POLICY "Users can update own profile" 
ON users FOR UPDATE 
USING (id = auth.uid());
```

```sql
-- POSTS: Anyone can read posts
CREATE POLICY "Posts are viewable by everyone" 
ON posts FOR SELECT 
USING (true);
```

```sql
-- POSTS: Anyone can create posts (for testing/development)
CREATE POLICY "Anyone can create posts" 
ON posts FOR INSERT 
WITH CHECK (true);
```

```sql
-- POSTS: Users can update their own posts
CREATE POLICY "Users can update own posts" 
ON posts FOR UPDATE 
USING (user_id = auth.uid()::text);
```

```sql
-- POSTS: Users can delete their own posts
CREATE POLICY "Users can delete own posts" 
ON posts FOR DELETE 
USING (user_id = auth.uid()::text);
```

```sql
-- POST LIKES: Anyone can view likes
CREATE POLICY "Anyone can view likes" 
ON post_likes FOR SELECT 
USING (true);
```

```sql
-- POST LIKES: Anyone can like posts (for anonymous/mock users)
CREATE POLICY "Anyone can like posts" 
ON post_likes FOR INSERT 
WITH CHECK (true);
```

```sql
-- POST LIKES: Users can remove their own likes
CREATE POLICY "Users can unlike posts" 
ON post_likes FOR DELETE 
USING (true);
```

```sql
-- CONVERSATIONS: Users can view conversations they participate in
CREATE POLICY "Users can view own conversations"
ON conversations FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM conversation_participants 
        WHERE conversation_participants.conversation_id = conversations.id 
        AND conversation_participants.user_id = auth.uid()
    )
);
```

```sql
-- CONVERSATION PARTICIPANTS: Users can view participants of their conversations
CREATE POLICY "Users can view conversation participants"
ON conversation_participants FOR SELECT
USING (
    conversation_participants.user_id = auth.uid() 
    OR EXISTS (
        SELECT 1 FROM conversation_participants cp2
        WHERE cp2.conversation_id = conversation_participants.conversation_id
        AND cp2.user_id = auth.uid()
    )
);
```

```sql
-- MESSAGES: Users can read messages in their conversations
CREATE POLICY "Users can read own messages" 
ON dm_messages FOR SELECT 
USING (
    EXISTS (
        SELECT 1 FROM conversation_participants cp
        WHERE cp.conversation_id = dm_messages.conversation_id 
        AND cp.user_id = auth.uid()
    )
);
```

```sql
-- MESSAGES: Users can send messages to their conversations
CREATE POLICY "Users can send messages" 
ON dm_messages FOR INSERT 
WITH CHECK (
    sender_id = auth.uid() AND
    EXISTS (
        SELECT 1 FROM conversation_participants cp
        WHERE cp.conversation_id = dm_messages.conversation_id 
        AND cp.user_id = auth.uid()
    )
);
```

```sql
-- MESSAGE REACTIONS: Users can view reactions on messages in their conversations
CREATE POLICY "Users can view reactions" 
ON message_reactions FOR SELECT 
USING (
    EXISTS (
        SELECT 1 FROM dm_messages m
        JOIN conversation_participants cp ON m.conversation_id = cp.conversation_id
        WHERE m.id = message_reactions.message_id 
        AND cp.user_id = auth.uid()
    )
);
```

```sql
-- MESSAGE REACTIONS: Users can add reactions to messages in their conversations
CREATE POLICY "Users can add reactions" 
ON message_reactions FOR INSERT 
WITH CHECK (
    message_reactions.user_id = auth.uid() AND
    EXISTS (
        SELECT 1 FROM dm_messages m
        JOIN conversation_participants cp ON m.conversation_id = cp.conversation_id
        WHERE m.id = message_reactions.message_id 
        AND cp.user_id = auth.uid()
    )
);
```

```sql
-- MESSAGE REACTIONS: Users can remove their own reactions
CREATE POLICY "Users can remove own reactions" 
ON message_reactions FOR DELETE 
USING (message_reactions.user_id = auth.uid());
```

```sql
-- STORIES: Anyone can view non-expired stories
CREATE POLICY "Active stories are viewable" 
ON stories FOR SELECT 
USING (expires_at > NOW());
```

```sql
-- STORIES: Users can create their own stories
CREATE POLICY "Users can create own stories" 
ON stories FOR INSERT 
WITH CHECK (user_id = auth.uid());
```

```sql
-- STORIES: Users can delete their own stories
CREATE POLICY "Users can delete own stories" 
ON stories FOR DELETE 
USING (user_id = auth.uid());
```

**Policies for new tables (run each one separately):**

```sql
-- FOLLOWS: Anyone can see follows
CREATE POLICY "Anyone can view follows" ON follows FOR SELECT USING (true);
```

```sql
-- FOLLOWS: Authenticated users can follow
CREATE POLICY "Authenticated users can follow" ON follows FOR INSERT
WITH CHECK (follower_id = auth.uid());
```

```sql
-- FOLLOWS: Users can unfollow
CREATE POLICY "Users can unfollow" ON follows FOR DELETE
USING (follower_id = auth.uid());
```

```sql
-- BLOCKED USERS: Users can view their own blocks
CREATE POLICY "Users can view own blocks" ON blocked_users FOR SELECT
USING (blocker_id = auth.uid());
```

```sql
-- BLOCKED USERS: Users can block
CREATE POLICY "Users can block" ON blocked_users FOR INSERT
WITH CHECK (blocker_id = auth.uid());
```

```sql
-- BLOCKED USERS: Users can unblock
CREATE POLICY "Users can unblock" ON blocked_users FOR DELETE
USING (blocker_id = auth.uid());
```

```sql
-- MUTED USERS: Users can view own mutes
CREATE POLICY "Users can view own mutes" ON muted_users FOR SELECT
USING (muter_id = auth.uid());
```

```sql
-- MUTED USERS: Users can mute
CREATE POLICY "Users can mute" ON muted_users FOR INSERT
WITH CHECK (muter_id = auth.uid());
```

```sql
-- MUTED USERS: Users can unmute
CREATE POLICY "Users can unmute" ON muted_users FOR DELETE
USING (muter_id = auth.uid());
```

```sql
-- BOOKMARKS: Users can view own bookmarks
CREATE POLICY "Users can view own bookmarks" ON bookmarks FOR SELECT
USING (user_id = auth.uid());
```

```sql
-- BOOKMARKS: Users can create bookmarks
CREATE POLICY "Users can create bookmarks" ON bookmarks FOR INSERT
WITH CHECK (user_id = auth.uid());
```

```sql
-- BOOKMARKS: Users can remove bookmarks
CREATE POLICY "Users can remove bookmarks" ON bookmarks FOR DELETE
USING (user_id = auth.uid());
```

```sql
-- NOTIFICATIONS: Users can view own notifications
CREATE POLICY "Users can view own notifications" ON notifications FOR SELECT
USING (user_id = auth.uid());
```

```sql
-- NOTIFICATIONS: System can create notifications (permissive for dev)
CREATE POLICY "Anyone can create notifications" ON notifications FOR INSERT
WITH CHECK (true);
```

```sql
-- NOTIFICATIONS: Users can update own notifications (mark read)
CREATE POLICY "Users can update own notifications" ON notifications FOR UPDATE
USING (user_id = auth.uid());
```

```sql
-- REPORTS: Authenticated users can create reports
CREATE POLICY "Anyone can create reports" ON reports FOR INSERT
WITH CHECK (true);
```

```sql
-- REPORTS: Users can view own reports
CREATE POLICY "Users can view own reports" ON reports FOR SELECT
USING (reporter_id = auth.uid());
```

```sql
-- POST COMMENTS: Anyone can view comments
CREATE POLICY "Anyone can view comments" ON post_comments FOR SELECT
USING (true);
```

```sql
-- POST COMMENTS: Anyone can create comments (permissive for dev)
CREATE POLICY "Anyone can create comments" ON post_comments FOR INSERT
WITH CHECK (true);
```

```sql
-- POST COMMENTS: Users can delete own comments
CREATE POLICY "Users can delete own comments" ON post_comments FOR DELETE
USING (user_id = auth.uid()::text);
```

### Troubleshooting RLS Policies

If you still get type mismatch errors, check your column types:

```sql
-- Check column types in your tables
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'id';

SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'posts' AND column_name = 'user_id';
```

If columns are `text` instead of `uuid`, use this format instead:
```sql
-- Example for text columns:
CREATE POLICY "example" ON table_name FOR SELECT 
USING (user_id = auth.uid()::text);
```

### 5.4 Verify Setup Works

Run these queries to verify your database is set up correctly:

#### Check Tables Exist
```sql
-- This should return all your tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
```

**Expected output:**
- conversation_participants
- conversations
- dm_messages
- message_reactions
- posts
- stories
- users

#### Check Column Types
```sql
-- Verify user_id columns are UUID type
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND column_name IN ('id', 'user_id', 'sender_id')
ORDER BY table_name, column_name;
```

#### Check RLS is Enabled
```sql
-- Should show all tables with RLS enabled (relrowsecurity = true)
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public';
```

#### Check Policies Exist
```sql
-- List all RLS policies
SELECT schemaname, tablename, policyname, cmd 
FROM pg_policies 
WHERE schemaname = 'public'
ORDER BY tablename, policyname;
```

#### Test Insert (Optional - Creates Test Data)
```sql
-- Insert a test user (only works if you're authenticated or policies allow)
INSERT INTO users (email, username, display_name) 
VALUES ('test@example.com', 'testuser', 'Test User')
RETURNING *;
```

If all queries work without errors, your database is ready! 🎉

### 5.5 (Optional) Create Helper Functions

```sql
-- Function to get conversation between two users
CREATE OR REPLACE FUNCTION get_or_create_conversation(user1_id UUID, user2_id UUID)
RETURNS UUID AS $$
DECLARE
    conv_id UUID;
BEGIN
    -- Check if conversation already exists
    SELECT cp1.conversation_id INTO conv_id
    FROM conversation_participants cp1
    JOIN conversation_participants cp2 ON cp1.conversation_id = cp2.conversation_id
    WHERE cp1.user_id = user1_id AND cp2.user_id = user2_id;
    
    -- If not, create new conversation
    IF conv_id IS NULL THEN
        INSERT INTO conversations DEFAULT VALUES RETURNING id INTO conv_id;
        INSERT INTO conversation_participants (conversation_id, user_id) VALUES (conv_id, user1_id);
        INSERT INTO conversation_participants (conversation_id, user_id) VALUES (conv_id, user2_id);
    END IF;
    
    RETURN conv_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
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

### 7.2 Test Core Features

| Feature | How to Test | Expected Result |
|---------|-------------|-----------------|
| Feed | Open app | See posts (mock or real) |
| Stories | Tap story circles | View stories |
| Create Post | Tap + button on feed | Post appears |
| DMs | Go to Messages tab | See conversations |
| Voice Call | Open DM → tap phone icon | Call UI appears |
| Video Call | Open DM → tap camera icon | Video call UI |
| Settings | Profile → Settings | All settings work |

### 7.3 Access Developer Options
1. Go to **Profile** → **Settings**
2. Scroll to bottom → tap "Version" 7 times (or similar easter egg)
3. Or use your `DEVELOPER_DEVICE_HASH` if configured

---

## 🔧 Quick Reference: All Configuration Files

| File | Purpose | Required? |
|------|---------|-----------|
| `local.properties` | Supabase keys, SDK path | Yes |
| `app/google-services.json` | Firebase config | Yes for push notifications |
| `MainActivity.kt` | RevenueCat key | Only for purchases |
| `SupabaseClient.kt` | Backend connection | Auto-configured |

---

## 🧪 Testing Without Backend

The app is designed to work without any backend configuration!

**Mock Data Mode** is enabled when:
- Supabase URL/Key not configured
- Network unavailable
- Backend errors occur

This lets you test all UI features without setting up services.

---

## 📞 Testing Mock Calls

Video and voice calls are fully mocked for testing:

1. Open any conversation
2. Tap 📹 (video) or 📞 (voice) icon
3. Call UI appears with:
   - Ringing animation (3 seconds)
   - Auto-connects
   - Duration timer
   - Mute/Speaker/Camera controls
   - End call button

---

## 💬 Message Reactions

NeuroComet supports modern message reactions like WhatsApp, Telegram, and iMessage:

### How to React to Messages

1. **Long-press** any message in a conversation
2. A reaction picker popup appears with quick reactions: ❤️ 👍 😂 😮 😢 🙏
3. Tap an emoji to react
4. Reactions appear below the message bubble

### Reaction Features

| Feature | Description |
|---------|-------------|
| Long-press to react | Hold any message to show reaction picker |
| Quick reactions | 6 preset emojis for fast reactions |
| Grouped display | Multiple reactions show with counts |
| Toggle reactions | Tap your reaction again to remove it |
| Haptic feedback | Vibration on long-press for tactile confirmation |

### Testing Reactions
Reactions work with mock data - no backend required for UI testing.
To persist reactions, ensure Supabase is configured.

---

## 🌍 Supported Languages

NeuroComet supports multiple languages for a global, neurodivergent-friendly experience:

### Currently Supported Languages

| Code | Language | Native Name |
|------|----------|-------------|
| `en` | English | English |
| `es` | Spanish | Español |
| `fr` | French | Français |
| `de` | German | Deutsch |
| `it` | Italian | Italiano |
| `pt` | Portuguese | Português |
| `nl` | Dutch | Nederlands |
| `pl` | Polish | Polski |
| `ru` | Russian | Русский |
| `uk` | Ukrainian | Українська |
| `ja` | Japanese | 日本語 |
| `ko` | Korean | 한국어 |
| `zh` | Chinese (Simplified) | 简体中文 |
| `zh-TW` | Chinese (Traditional) | 繁體中文 |
| `ar` | Arabic | العربية |
| `hi` | Hindi | हिन्दी |
| `tr` | Turkish | Türkçe |
| `vi` | Vietnamese | Tiếng Việt |
| `th` | Thai | ไทย |
| `id` | Indonesian | Bahasa Indonesia |

### How Language Settings Work

1. **Automatic Detection**: App uses device language by default
2. **Manual Override**: Go to **Settings** → **Theme Settings** to change language
3. **Persistent Storage**: Language preference saved in `local.properties`

### Adding New Language Translations

To add a new language:

1. Create a new `values-XX` folder in `app/src/main/res/` (where XX is the language code)
2. Copy `strings.xml` from `values` folder
3. Translate all string resources
4. Rebuild the app

Example for adding Swedish (sv):
```
app/src/main/res/values-sv/strings.xml
```

### RTL (Right-to-Left) Support

Languages like Arabic (`ar`) and Hebrew (`he`) are fully supported with:
- RTL text direction
- Mirrored layouts
- Proper navigation flow

---

## 🚨 Troubleshooting

### Build Fails
```bash
# Clean and rebuild
.\gradlew clean assembleDebug
```

### Supabase Not Connecting
1. Check `local.properties` has correct URL and KEY
2. Ensure no extra quotes around values
3. Rebuild after changing properties

### Firebase Errors
- Ensure `google-services.json` matches your package name
- Native Android release package: `com.kyilmaz.neurocomet`
- Native Android debug package: `com.kyilmaz.neurocomet.debug`

### App Crashes on Start
- Check Logcat for the specific error
- Common: Missing permissions in manifest
- Try: **Build** → **Clean Project** → **Rebuild**

---

## 📝 Summary Checklist

Before testing, ensure you've completed:

- [ ] **local.properties** - Added Supabase URL and KEY
- [ ] **google-services.json** - Downloaded from Firebase
- [ ] **RevenueCat** - Added API key (or skip for basic testing)
- [ ] **Supabase Tables** - Created schema (or use mock data)
- [ ] **Gradle Sync** - Project synced successfully
- [ ] **Build** - App builds without errors

---

**You're ready to test!** 🎉

The app will work with mock data if backends aren't configured, so you can test UI features immediately.
