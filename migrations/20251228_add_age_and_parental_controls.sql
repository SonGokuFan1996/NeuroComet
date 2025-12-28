-- Migration: Add age and parental controls support
-- Date: 2025-12-28
-- Description: Adds birthdate and parent_id to users table, creates parental_controls table

-- Add birthdate column to users table to support age calculation
ALTER TABLE users ADD COLUMN birthdate DATE NULL;

-- Add parent_id column to users table to establish parent-child relationships
-- This allows linking child accounts to their parent accounts
ALTER TABLE users ADD COLUMN parent_id BIGINT NULL;

-- Add foreign key constraint for parent_id
ALTER TABLE users 
ADD CONSTRAINT fk_users_parent 
FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create index on parent_id for efficient queries of children by parent
CREATE INDEX idx_users_parent_id ON users(parent_id);

-- Create parental_controls table to store parental control settings
CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  
  -- Foreign key to users table (the child user)
  user_id BIGINT NOT NULL UNIQUE,
  
  -- Whether parental controls are enabled for this user
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  
  -- Whether the child is allowed to use chat features
  allow_chat BOOLEAN NOT NULL DEFAULT FALSE,
  
  -- Whether the child's profile can be public
  allow_profile_public BOOLEAN NOT NULL DEFAULT FALSE,
  
  -- JSON array of whitelisted contacts/domains
  -- Example: ["user123", "user456"] for allowed users
  whitelist JSONB NOT NULL DEFAULT '[]'::jsonb,
  
  -- Timestamps
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  -- Foreign key constraint
  CONSTRAINT fk_parental_controls_user
    FOREIGN KEY (user_id) 
    REFERENCES users(id) 
    ON DELETE CASCADE
);

-- Create index on user_id for efficient lookups
CREATE INDEX idx_parental_controls_user_id ON parental_controls(user_id);

-- Add comment documentation
COMMENT ON TABLE parental_controls IS 'Stores parental control settings for child users under 13 or requiring supervision';
COMMENT ON COLUMN users.birthdate IS 'User birthdate for age calculation and COPPA compliance';
COMMENT ON COLUMN users.parent_id IS 'References parent user ID for child accounts';
COMMENT ON COLUMN parental_controls.enabled IS 'Master switch for parental controls';
COMMENT ON COLUMN parental_controls.allow_chat IS 'Whether child can access chat features';
COMMENT ON COLUMN parental_controls.allow_profile_public IS 'Whether child profile can be publicly visible';
COMMENT ON COLUMN parental_controls.whitelist IS 'JSON array of whitelisted user IDs or domains';
