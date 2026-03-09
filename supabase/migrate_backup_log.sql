-- Migration: Add backup tracking table
-- Tracks user backup history for server-side awareness

CREATE TABLE IF NOT EXISTS user_backup_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  backup_id TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  storage_type TEXT NOT NULL DEFAULT 'local' CHECK (storage_type IN ('local', 'googleDrive')),
  is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
  app_version TEXT,
  data_manifest JSONB
);

-- Index for fast lookups by user
CREATE INDEX IF NOT EXISTS idx_user_backup_log_user_id ON user_backup_log(user_id);
CREATE INDEX IF NOT EXISTS idx_user_backup_log_created_at ON user_backup_log(created_at DESC);

-- RLS policies
ALTER TABLE user_backup_log ENABLE ROW LEVEL SECURITY;

-- Users can only see their own backup logs
CREATE POLICY "Users can view own backup logs"
  ON user_backup_log FOR SELECT
  USING (auth.uid() = user_id);

-- Users can insert their own backup logs
CREATE POLICY "Users can insert own backup logs"
  ON user_backup_log FOR INSERT
  WITH CHECK (auth.uid() = user_id);

-- Users can delete their own backup logs
CREATE POLICY "Users can delete own backup logs"
  ON user_backup_log FOR DELETE
  USING (auth.uid() = user_id);

