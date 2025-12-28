-- Migration: Add age and parental controls support
-- Date: 2025-12-28
-- Description: Adds birthdate and parent_id fields to users table and creates parental_controls table

-- Add birthdate column to users table
ALTER TABLE users ADD COLUMN birthdate DATE NULL;

-- Add parent_id column to users table for linking child accounts to parent accounts
ALTER TABLE users ADD COLUMN parent_id BIGINT NULL;

-- Add foreign key constraint for parent_id
-- Note: Adjust constraint name and table name as needed for your schema
ALTER TABLE users 
  ADD CONSTRAINT fk_users_parent_id 
  FOREIGN KEY (parent_id) REFERENCES users(id) 
  ON DELETE SET NULL;

-- Create index on parent_id for efficient queries
CREATE INDEX idx_users_parent_id ON users(parent_id);

-- Create parental_controls table
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  allow_chat BOOLEAN NOT NULL DEFAULT FALSE,
  allow_profile_public BOOLEAN NOT NULL DEFAULT FALSE,
  whitelist JSONB DEFAULT '[]'::jsonb,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Foreign key to users table
  CONSTRAINT fk_parental_controls_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE
);

-- Create index on user_id for efficient lookups
CREATE INDEX idx_parental_controls_user_id ON parental_controls(user_id);

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_parental_controls_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_parental_controls_updated_at
  BEFORE UPDATE ON parental_controls
  FOR EACH ROW
  EXECUTE FUNCTION update_parental_controls_updated_at();

-- Add comments for documentation
COMMENT ON COLUMN users.birthdate IS 'User birthdate for age verification and content filtering';
COMMENT ON COLUMN users.parent_id IS 'Parent user ID for child accounts (under 13)';
COMMENT ON TABLE parental_controls IS 'Parental control settings for child accounts';
COMMENT ON COLUMN parental_controls.enabled IS 'Whether parental controls are enabled';
COMMENT ON COLUMN parental_controls.allow_chat IS 'Whether child is allowed to use chat features';
COMMENT ON COLUMN parental_controls.allow_profile_public IS 'Whether child profile can be public';
COMMENT ON COLUMN parental_controls.whitelist IS 'JSON array of whitelisted content/users';
