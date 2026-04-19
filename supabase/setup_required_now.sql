-- ============================================================================
-- NeuroComet: minimum required Supabase setup for the app to work now
--
-- Run this in Supabase SQL Editor as one script.
-- It is written to be safe to re-run.
--
-- Covers:
--   - auth-compatible users + profiles
--   - feed/social tables used by Android + Flutter
--   - messaging tables + missing dm_messages.read_at column
--   - calling tables
--   - indexes, RLS, core policies, realtime, and signup trigger
--
-- Notes:
--   - The current app uses BOTH public.users and public.profiles.
--   - public.users is still needed by feed/profile/search flows.
--   - public.profiles is still needed by messaging display data.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. CORE TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    username TEXT UNIQUE,
    display_name TEXT,
    avatar_url TEXT,
    banner_url TEXT,
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    deletion_scheduled_at TIMESTAMPTZ,
    detox_started_at TIMESTAMPTZ,
    detox_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON public.users(username);

CREATE TABLE IF NOT EXISTS public.posts (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT,
    content TEXT NOT NULL,
    image_url TEXT,
    video_url TEXT,
    likes INTEGER NOT NULL DEFAULT 0,
    comments INTEGER NOT NULL DEFAULT 0,
    shares INTEGER NOT NULL DEFAULT 0,
    category TEXT,
    media_items JSONB,
    min_audience TEXT DEFAULT 'UNDER_13',
    background_color BIGINT,
    is_liked_by_me BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON public.posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON public.posts(created_at DESC);

CREATE TABLE IF NOT EXISTS public.post_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id BIGINT NOT NULL,
    user_id TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(post_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_post_likes_post ON public.post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_user ON public.post_likes(user_id);

CREATE TABLE IF NOT EXISTS public.post_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id BIGINT NOT NULL,
    user_id TEXT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comments_post ON public.post_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user ON public.post_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON public.post_comments(parent_comment_id);

-- ============================================================================
-- 2. MESSAGING + PROFILES
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    is_group BOOLEAN NOT NULL DEFAULT false,
    group_name TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.conversation_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(conversation_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_conv_participants_conv ON public.conversation_participants(conversation_id);
CREATE INDEX IF NOT EXISTS idx_conv_participants_user ON public.conversation_participants(user_id);

CREATE TABLE IF NOT EXISTS public.dm_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'text',
    media_url TEXT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_dm_messages_conv ON public.dm_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_dm_messages_sender ON public.dm_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_dm_messages_created ON public.dm_messages(conversation_id, created_at DESC);

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT,
    username TEXT UNIQUE,
    avatar_url TEXT,
    bio TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Backfill missing columns if tables already existed in an older shape.
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS banner_url TEXT;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS deletion_scheduled_at TIMESTAMPTZ;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS detox_started_at TIMESTAMPTZ;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS detox_until TIMESTAMPTZ;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS comments INTEGER NOT NULL DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS shares INTEGER NOT NULL DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS category TEXT;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS is_liked_by_me BOOLEAN DEFAULT false;
ALTER TABLE public.dm_messages ADD COLUMN IF NOT EXISTS type TEXT NOT NULL DEFAULT 'text';
ALTER TABLE public.dm_messages ADD COLUMN IF NOT EXISTS media_url TEXT;
ALTER TABLE public.dm_messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.dm_messages ADD COLUMN IF NOT EXISTS read_at TIMESTAMPTZ;

-- Keep public.users and public.profiles in sync for auth signups.
CREATE OR REPLACE FUNCTION public.handle_new_auth_user_neurocomet()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.users (id, email, username, display_name, created_at, updated_at)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'username', 'user_' || substr(NEW.id::text, 1, 8)),
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(COALESCE(NEW.email, ''), '@', 1)),
        NOW(),
        NOW()
    )
    ON CONFLICT (id) DO UPDATE
    SET
        email = COALESCE(EXCLUDED.email, public.users.email),
        updated_at = NOW();

    INSERT INTO public.profiles (id, display_name, username, created_at, updated_at)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(COALESCE(NEW.email, ''), '@', 1)),
        COALESCE(NEW.raw_user_meta_data->>'username', 'user_' || substr(NEW.id::text, 1, 8)),
        NOW(),
        NOW()
    )
    ON CONFLICT (id) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created_neurocomet ON auth.users;
CREATE TRIGGER on_auth_user_created_neurocomet
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_auth_user_neurocomet();

-- Backfill existing auth users so current accounts also get public rows.
INSERT INTO public.users (id, email, username, display_name, created_at, updated_at)
SELECT
    au.id,
    au.email,
    COALESCE(au.raw_user_meta_data->>'username', 'user_' || substr(au.id::text, 1, 8)),
    COALESCE(au.raw_user_meta_data->>'display_name', split_part(COALESCE(au.email, ''), '@', 1)),
    COALESCE(au.created_at, NOW()),
    NOW()
FROM auth.users au
ON CONFLICT (id) DO UPDATE
SET
    email = COALESCE(EXCLUDED.email, public.users.email),
    updated_at = NOW();

INSERT INTO public.profiles (id, display_name, username, created_at, updated_at)
SELECT
    au.id,
    COALESCE(au.raw_user_meta_data->>'display_name', split_part(COALESCE(au.email, ''), '@', 1)),
    COALESCE(au.raw_user_meta_data->>'username', 'user_' || substr(au.id::text, 1, 8)),
    COALESCE(au.created_at, NOW()),
    NOW()
FROM auth.users au
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 3. SOCIAL / MODERATION / NOTIFICATIONS
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_follower ON public.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON public.follows(following_id);

CREATE TABLE IF NOT EXISTS public.blocked_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);
CREATE INDEX IF NOT EXISTS idx_blocked_blocker ON public.blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_blocked ON public.blocked_users(blocked_id);

CREATE TABLE IF NOT EXISTS public.muted_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    muter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    muted_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(muter_id, muted_id)
);
CREATE INDEX IF NOT EXISTS idx_muted_muter ON public.muted_users(muter_id);

CREATE TABLE IF NOT EXISTS public.bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON public.bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_post ON public.bookmarks(post_id);

CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    type TEXT NOT NULL,
    message TEXT,
    target_id TEXT,
    target_type TEXT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON public.notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON public.notifications(created_at DESC);

CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    content_type TEXT NOT NULL,
    content_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    additional_info TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_reports_status ON public.reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_reporter ON public.reports(reporter_id);

-- ============================================================================
-- 4. CALLING
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.call_signals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_id TEXT NOT NULL,
    from_user_id TEXT NOT NULL,
    to_user_id TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('offer', 'answer', 'ice_candidate', 'call_request', 'call_accept', 'call_decline', 'call_end')),
    payload TEXT DEFAULT '',
    call_type TEXT NOT NULL DEFAULT 'VOICE' CHECK (call_type IN ('VOICE', 'VIDEO')),
    caller_name TEXT DEFAULT 'Unknown',
    caller_avatar TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_call_signals_to_user ON public.call_signals(to_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_call_signals_created_at ON public.call_signals(created_at);

CREATE TABLE IF NOT EXISTS public.call_history (
    id TEXT PRIMARY KEY DEFAULT 'call_' || extract(epoch from now())::bigint::text || '_' || gen_random_uuid()::text,
    caller_id TEXT NOT NULL,
    recipient_id TEXT NOT NULL,
    recipient_name TEXT DEFAULT 'Unknown',
    recipient_avatar TEXT DEFAULT '',
    call_type TEXT NOT NULL DEFAULT 'VOICE' CHECK (call_type IN ('VOICE', 'VIDEO')),
    is_outgoing BOOLEAN NOT NULL DEFAULT true,
    outcome TEXT NOT NULL DEFAULT 'COMPLETED' CHECK (outcome IN ('COMPLETED', 'MISSED', 'DECLINED', 'NO_ANSWER', 'CANCELLED', 'FAILED')),
    duration_seconds BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_call_history_caller ON public.call_history(caller_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_call_history_recipient ON public.call_history(recipient_id, created_at DESC);

CREATE TABLE IF NOT EXISTS public.stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    image_url TEXT,
    video_url TEXT,
    duration INTEGER DEFAULT 5000,
    content_type TEXT DEFAULT 'IMAGE',
    text_overlay TEXT,
    background_color BIGINT DEFAULT 4279917102,
    background_color_end BIGINT,
    link_preview JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '24 hours')
);
CREATE INDEX IF NOT EXISTS idx_stories_user_id ON public.stories(user_id);
CREATE INDEX IF NOT EXISTS idx_stories_expires_at ON public.stories(expires_at);

-- ============================================================================
-- 5. RLS
-- ============================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.post_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.post_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.dm_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.muted_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.call_signals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.call_history ENABLE ROW LEVEL SECURITY;

-- Drop/recreate policies so the script stays re-runnable.
DROP POLICY IF EXISTS "Public profiles are viewable by everyone" ON public.users;
DROP POLICY IF EXISTS "Anyone can create users" ON public.users;
DROP POLICY IF EXISTS "Users can update own profile" ON public.users;

DROP POLICY IF EXISTS "Profiles are viewable by everyone" ON public.profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;

DROP POLICY IF EXISTS "Posts are viewable by everyone" ON public.posts;
DROP POLICY IF EXISTS "Anyone can create posts" ON public.posts;
DROP POLICY IF EXISTS "Users can update own posts" ON public.posts;
DROP POLICY IF EXISTS "Users can delete own posts" ON public.posts;

DROP POLICY IF EXISTS "Anyone can view likes" ON public.post_likes;
DROP POLICY IF EXISTS "Anyone can like posts" ON public.post_likes;
DROP POLICY IF EXISTS "Users can unlike posts" ON public.post_likes;

DROP POLICY IF EXISTS "Anyone can view comments" ON public.post_comments;
DROP POLICY IF EXISTS "Anyone can create comments" ON public.post_comments;
DROP POLICY IF EXISTS "Users can delete own comments" ON public.post_comments;

DROP POLICY IF EXISTS "Users can view their conversations" ON public.conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON public.conversations;

DROP POLICY IF EXISTS "Users can view conversation participants" ON public.conversation_participants;
DROP POLICY IF EXISTS "Users can add participants" ON public.conversation_participants;

DROP POLICY IF EXISTS "Users can view messages in their conversations" ON public.dm_messages;
DROP POLICY IF EXISTS "Users can send messages to their conversations" ON public.dm_messages;
DROP POLICY IF EXISTS "Users can update message read status" ON public.dm_messages;

DROP POLICY IF EXISTS "Anyone can view follows" ON public.follows;
DROP POLICY IF EXISTS "Authenticated users can follow" ON public.follows;
DROP POLICY IF EXISTS "Users can unfollow" ON public.follows;

DROP POLICY IF EXISTS "Users can view own blocks" ON public.blocked_users;
DROP POLICY IF EXISTS "Users can block" ON public.blocked_users;
DROP POLICY IF EXISTS "Users can unblock" ON public.blocked_users;

DROP POLICY IF EXISTS "Users can view own mutes" ON public.muted_users;
DROP POLICY IF EXISTS "Users can mute" ON public.muted_users;
DROP POLICY IF EXISTS "Users can unmute" ON public.muted_users;

DROP POLICY IF EXISTS "Users can view own bookmarks" ON public.bookmarks;
DROP POLICY IF EXISTS "Users can create bookmarks" ON public.bookmarks;
DROP POLICY IF EXISTS "Users can remove bookmarks" ON public.bookmarks;

DROP POLICY IF EXISTS "Users can view own notifications" ON public.notifications;
DROP POLICY IF EXISTS "Anyone can create notifications" ON public.notifications;
DROP POLICY IF EXISTS "Users can update own notifications" ON public.notifications;

DROP POLICY IF EXISTS "Anyone can create reports" ON public.reports;
DROP POLICY IF EXISTS "Users can view own reports" ON public.reports;

DROP POLICY IF EXISTS "Users can insert call signals" ON public.call_signals;
DROP POLICY IF EXISTS "Users can read their own call signals" ON public.call_signals;
DROP POLICY IF EXISTS "Users can insert their call history" ON public.call_history;
DROP POLICY IF EXISTS "Users can read their call history" ON public.call_history;
DROP POLICY IF EXISTS "Users can delete their call history" ON public.call_history;

DROP POLICY IF EXISTS "Stories are viewable by everyone" ON public.stories;
DROP POLICY IF EXISTS "Users can create own stories" ON public.stories;
DROP POLICY IF EXISTS "Users can delete own stories" ON public.stories;

CREATE POLICY "Public profiles are viewable by everyone"
    ON public.users FOR SELECT
    USING (true);

CREATE POLICY "Anyone can create users"
    ON public.users FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update own profile"
    ON public.users FOR UPDATE
    USING (id = auth.uid())
    WITH CHECK (id = auth.uid());

CREATE POLICY "Profiles are viewable by everyone"
    ON public.profiles FOR SELECT
    USING (true);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Posts are viewable by everyone"
    ON public.posts FOR SELECT
    USING (true);

CREATE POLICY "Anyone can create posts"
    ON public.posts FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update own posts"
    ON public.posts FOR UPDATE
    USING (user_id = auth.uid()::text)
    WITH CHECK (user_id = auth.uid()::text);

CREATE POLICY "Users can delete own posts"
    ON public.posts FOR DELETE
    USING (user_id = auth.uid()::text);

CREATE POLICY "Anyone can view likes"
    ON public.post_likes FOR SELECT
    USING (true);

CREATE POLICY "Anyone can like posts"
    ON public.post_likes FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can unlike posts"
    ON public.post_likes FOR DELETE
    USING (true);

CREATE POLICY "Anyone can view comments"
    ON public.post_comments FOR SELECT
    USING (true);

CREATE POLICY "Anyone can create comments"
    ON public.post_comments FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can delete own comments"
    ON public.post_comments FOR DELETE
    USING (user_id = auth.uid()::text);

-- Helper: SECURITY DEFINER function checks conversation membership without
-- triggering RLS on conversation_participants (prevents infinite recursion).
CREATE OR REPLACE FUNCTION public.is_conversation_member(conv_id UUID)
RETURNS BOOLEAN
LANGUAGE sql STABLE SECURITY DEFINER
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.conversation_participants
        WHERE conversation_id = conv_id AND user_id = auth.uid()
    );
$$;

CREATE POLICY "Users can view their conversations"
    ON public.conversations FOR SELECT
    USING (public.is_conversation_member(id));

CREATE POLICY "Users can create conversations"
    ON public.conversations FOR INSERT
    WITH CHECK (true);

-- Simple direct check — no self-referential subquery
CREATE POLICY "Users can view conversation participants"
    ON public.conversation_participants FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Users can add participants"
    ON public.conversation_participants FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can view messages in their conversations"
    ON public.dm_messages FOR SELECT
    USING (public.is_conversation_member(conversation_id));

CREATE POLICY "Users can send messages to their conversations"
    ON public.dm_messages FOR INSERT
    WITH CHECK (
        auth.uid() = sender_id
        AND public.is_conversation_member(conversation_id)
    );

CREATE POLICY "Users can update message read status"
    ON public.dm_messages FOR UPDATE
    USING  (public.is_conversation_member(conversation_id))
    WITH CHECK (public.is_conversation_member(conversation_id));

CREATE POLICY "Anyone can view follows"
    ON public.follows FOR SELECT
    USING (true);

CREATE POLICY "Authenticated users can follow"
    ON public.follows FOR INSERT
    WITH CHECK (follower_id = auth.uid());

CREATE POLICY "Users can unfollow"
    ON public.follows FOR DELETE
    USING (follower_id = auth.uid());

CREATE POLICY "Users can view own blocks"
    ON public.blocked_users FOR SELECT
    USING (blocker_id = auth.uid());

CREATE POLICY "Users can block"
    ON public.blocked_users FOR INSERT
    WITH CHECK (blocker_id = auth.uid());

CREATE POLICY "Users can unblock"
    ON public.blocked_users FOR DELETE
    USING (blocker_id = auth.uid());

CREATE POLICY "Users can view own mutes"
    ON public.muted_users FOR SELECT
    USING (muter_id = auth.uid());

CREATE POLICY "Users can mute"
    ON public.muted_users FOR INSERT
    WITH CHECK (muter_id = auth.uid());

CREATE POLICY "Users can unmute"
    ON public.muted_users FOR DELETE
    USING (muter_id = auth.uid());

CREATE POLICY "Users can view own bookmarks"
    ON public.bookmarks FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Users can create bookmarks"
    ON public.bookmarks FOR INSERT
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can remove bookmarks"
    ON public.bookmarks FOR DELETE
    USING (user_id = auth.uid());

CREATE POLICY "Users can view own notifications"
    ON public.notifications FOR SELECT
    USING (user_id = auth.uid());

CREATE POLICY "Anyone can create notifications"
    ON public.notifications FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update own notifications"
    ON public.notifications FOR UPDATE
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Anyone can create reports"
    ON public.reports FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can view own reports"
    ON public.reports FOR SELECT
    USING (reporter_id = auth.uid());

CREATE POLICY "Users can insert call signals"
    ON public.call_signals FOR INSERT
    WITH CHECK (auth.uid()::text = from_user_id);

CREATE POLICY "Users can read their own call signals"
    ON public.call_signals FOR SELECT
    USING (auth.uid()::text = to_user_id OR auth.uid()::text = from_user_id);

CREATE POLICY "Users can insert their call history"
    ON public.call_history FOR INSERT
    WITH CHECK (auth.uid()::text = caller_id);

CREATE POLICY "Users can read their call history"
    ON public.call_history FOR SELECT
    USING (auth.uid()::text = caller_id OR auth.uid()::text = recipient_id);

CREATE POLICY "Users can delete their call history"
    ON public.call_history FOR DELETE
    USING (auth.uid()::text = caller_id);

CREATE POLICY "Stories are viewable by everyone"
    ON public.stories FOR SELECT
    USING (true);

CREATE POLICY "Users can create own stories"
    ON public.stories FOR INSERT
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can delete own stories"
    ON public.stories FOR DELETE
    USING (user_id = auth.uid());

-- ============================================================================
-- 6. REALTIME + MESSAGE/TRIGGER HELPERS
-- ============================================================================

CREATE OR REPLACE FUNCTION public.update_conversation_timestamp()
RETURNS trigger AS $$
BEGIN
    UPDATE public.conversations
    SET updated_at = NOW()
    WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_new_message ON public.dm_messages;
CREATE TRIGGER on_new_message
    AFTER INSERT ON public.dm_messages
    FOR EACH ROW EXECUTE FUNCTION public.update_conversation_timestamp();

CREATE OR REPLACE FUNCTION public.cleanup_stale_call_signals()
RETURNS void AS $$
BEGIN
    DELETE FROM public.call_signals
    WHERE created_at < NOW() - INTERVAL '5 minutes';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'supabase_realtime') THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_publication_tables
            WHERE pubname = 'supabase_realtime'
              AND schemaname = 'public'
              AND tablename = 'dm_messages'
        ) THEN
            ALTER PUBLICATION supabase_realtime ADD TABLE public.dm_messages;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_publication_tables
            WHERE pubname = 'supabase_realtime'
              AND schemaname = 'public'
              AND tablename = 'call_signals'
        ) THEN
            ALTER PUBLICATION supabase_realtime ADD TABLE public.call_signals;
        END IF;
    END IF;
END $$;

-- ============================================================================
-- 7. GRANTS — required for PostgREST (anon / authenticated) access
-- ============================================================================
-- Without these, PostgREST returns 404 (table invisible) or 500 (RLS
-- policy subquery fails because a referenced table is invisible to the role).

GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO anon, authenticated, service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO anon, authenticated, service_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.users               TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.profiles            TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.posts               TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.post_likes          TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.post_comments       TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.conversations       TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.conversation_participants TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.dm_messages         TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.follows             TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.blocked_users       TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.muted_users         TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.bookmarks           TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.notifications       TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.reports             TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.call_signals        TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.call_history        TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.stories             TO anon, authenticated;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;

-- ============================================================================
-- 8. OPTIONAL BUT NORMALLY NEEDED NEXT
-- ============================================================================
-- Create these storage buckets in the Supabase dashboard for uploads:
--   - profile-media
--   - post-media
--
-- If you want, create them in Storage > Buckets and make them public for now.
-- Tighten bucket policies later after the app flow is confirmed working.
-- ============================================================================

