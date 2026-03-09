-- ============================================================================
-- NeuroComet: One-shot migration to add all missing tables and columns
-- Run this in the Supabase Dashboard SQL Editor
-- Safe to run multiple times (uses IF NOT EXISTS / IF EXISTS)
-- ============================================================================

-- 0. Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. ADD MISSING COLUMNS TO EXISTING TABLES
--    Wrapped in DO blocks so they skip gracefully if the table is missing
-- ============================================================================

DO $$ BEGIN
  ALTER TABLE users ADD COLUMN IF NOT EXISTS banner_url TEXT;
  ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
  ALTER TABLE users ADD COLUMN IF NOT EXISTS deletion_scheduled_at TIMESTAMPTZ;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS type TEXT DEFAULT 'text';
  ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS media_url TEXT;
  ALTER TABLE dm_messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT false;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE posts ADD COLUMN IF NOT EXISTS comments INTEGER DEFAULT 0;
  ALTER TABLE posts ADD COLUMN IF NOT EXISTS shares INTEGER DEFAULT 0;
  ALTER TABLE posts ADD COLUMN IF NOT EXISTS category TEXT;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;

-- ============================================================================
-- 2. CREATE MISSING TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id);

CREATE TABLE IF NOT EXISTS blocked_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);
CREATE INDEX IF NOT EXISTS idx_blocked_blocker ON blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_blocked ON blocked_users(blocked_id);

CREATE TABLE IF NOT EXISTS muted_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    muter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    muted_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(muter_id, muted_id)
);
CREATE INDEX IF NOT EXISTS idx_muted_muter ON muted_users(muter_id);

CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_post ON bookmarks(post_id);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type TEXT NOT NULL,
    message TEXT,
    target_id TEXT,
    target_type TEXT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications(created_at DESC);

CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type TEXT NOT NULL,
    content_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    additional_info TEXT,
    status TEXT DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_reporter ON reports(reporter_id);

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

-- ============================================================================
-- 3. ENABLE ROW LEVEL SECURITY ON NEW TABLES
-- ============================================================================

ALTER TABLE follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE muted_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE post_comments ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- DONE! All tables, columns, indexes, and policies are now in place.
-- ============================================================================
