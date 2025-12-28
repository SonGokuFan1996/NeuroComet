-- Migration: Add age and parental controls support
-- Date: 2025-12-28
-- Description: Adds birthdate and parent_id fields to users table, and creates parental_controls table

-- Add birthdate column to users table for age computation
ALTER TABLE users ADD COLUMN birthdate DATE NULL;

-- Add parent_id column to users table to establish parent-child relationships
ALTER TABLE users ADD COLUMN parent_id BIGINT NULL;

-- Add foreign key constraint for parent_id (self-referential)
ALTER TABLE users ADD CONSTRAINT fk_users_parent 
  FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create index on parent_id for efficient parent-child lookups
CREATE INDEX idx_users_parent_id ON users(parent_id);

-- Create parental_controls table
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  enabled BOOLEAN NOT NULL DEFAULT true,
  allow_chat BOOLEAN NOT NULL DEFAULT false,
  allow_profile_public BOOLEAN NOT NULL DEFAULT false,
  whitelist JSONB NOT NULL DEFAULT '[]'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  -- Foreign key constraint to users table
  CONSTRAINT fk_parental_controls_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on user_id for efficient lookups
CREATE INDEX idx_parental_controls_user_id ON parental_controls(user_id);

-- Add comment to table
COMMENT ON TABLE parental_controls IS 'Stores parental control settings for child users (under 13)';

-- Add comments to columns
COMMENT ON COLUMN parental_controls.enabled IS 'Whether parental controls are enabled for this user';
COMMENT ON COLUMN parental_controls.allow_chat IS 'Whether the child is allowed to use chat features';
COMMENT ON COLUMN parental_controls.allow_profile_public IS 'Whether the child profile can be publicly visible';
COMMENT ON COLUMN parental_controls.whitelist IS 'JSON array of approved usernames or content domains';
