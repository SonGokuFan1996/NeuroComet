-- Migration: Add age and parental controls support
-- Date: 2025-12-28
-- Description: Adds parental_controls table, indexes, and triggers for under-13 user support

-- Create parental_controls table if it doesn't exist
CREATE TABLE IF NOT EXISTS parental_controls (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  parent_id VARCHAR(255) NOT NULL,
  content_restriction_level VARCHAR(50) DEFAULT 'strict',
  allowed_content_types TEXT[], -- Array of allowed content categories
  screen_time_limit INTEGER, -- Screen time limit in minutes
  require_approval BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on user_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_parental_controls_user_id ON parental_controls(user_id);

-- Create index on parent_id for parent queries
CREATE INDEX IF NOT EXISTS idx_parental_controls_parent_id ON parental_controls(parent_id);

-- Create composite index for user_id and parent_id
CREATE INDEX IF NOT EXISTS idx_parental_controls_user_parent ON parental_controls(user_id, parent_id);

-- Create trigger function to maintain updated_at timestamp
CREATE OR REPLACE FUNCTION update_parental_controls_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
DROP TRIGGER IF EXISTS trigger_update_parental_controls_updated_at ON parental_controls;
CREATE TRIGGER trigger_update_parental_controls_updated_at
  BEFORE UPDATE ON parental_controls
  FOR EACH ROW
  EXECUTE FUNCTION update_parental_controls_updated_at();

-- Add birthdate column to users table if it doesn't exist
-- Note: This assumes a users table exists. Adjust table name as needed.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS birthdate DATE;
    CREATE INDEX IF NOT EXISTS idx_users_birthdate ON users(birthdate);
  END IF;
END $$;
