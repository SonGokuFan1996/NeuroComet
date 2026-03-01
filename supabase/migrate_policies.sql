-- ============================================================================
-- NeuroComet: RLS Policies for new tables
-- Run this AFTER migrate_missing_tables.sql completes successfully
-- ============================================================================

-- 1. Drop existing policies (safe even if they do not exist)

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

-- 2. Create policies

-- follows
CREATE POLICY "Anyone can view follows"
  ON follows FOR SELECT
  USING (true);

CREATE POLICY "Authenticated users can follow"
  ON follows FOR INSERT
  WITH CHECK (follower_id = auth.uid());

CREATE POLICY "Users can unfollow"
  ON follows FOR DELETE
  USING (follower_id = auth.uid());

-- blocked_users
CREATE POLICY "Users can view own blocks"
  ON blocked_users FOR SELECT
  USING (blocker_id = auth.uid());

CREATE POLICY "Users can block"
  ON blocked_users FOR INSERT
  WITH CHECK (blocker_id = auth.uid());

CREATE POLICY "Users can unblock"
  ON blocked_users FOR DELETE
  USING (blocker_id = auth.uid());

-- muted_users
CREATE POLICY "Users can view own mutes"
  ON muted_users FOR SELECT
  USING (muter_id = auth.uid());

CREATE POLICY "Users can mute"
  ON muted_users FOR INSERT
  WITH CHECK (muter_id = auth.uid());

CREATE POLICY "Users can unmute"
  ON muted_users FOR DELETE
  USING (muter_id = auth.uid());

-- bookmarks
CREATE POLICY "Users can view own bookmarks"
  ON bookmarks FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Users can create bookmarks"
  ON bookmarks FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can remove bookmarks"
  ON bookmarks FOR DELETE
  USING (user_id = auth.uid());

-- notifications
CREATE POLICY "Users can view own notifications"
  ON notifications FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Anyone can create notifications"
  ON notifications FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can update own notifications"
  ON notifications FOR UPDATE
  USING (user_id = auth.uid());

-- reports
CREATE POLICY "Anyone can create reports"
  ON reports FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can view own reports"
  ON reports FOR SELECT
  USING (reporter_id = auth.uid());

-- post_comments
CREATE POLICY "Anyone can view comments"
  ON post_comments FOR SELECT
  USING (true);

CREATE POLICY "Anyone can create comments"
  ON post_comments FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can delete own comments"
  ON post_comments FOR DELETE
  USING (user_id = auth.uid()::text);

