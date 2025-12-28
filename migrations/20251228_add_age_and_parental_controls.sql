-- SQL migration: add birthdate to users and parental_controls table
-- Adapt to your DB/ORM migration tooling (knex/Sequelize/flask-migrate)

ALTER TABLE users
  ADD COLUMN birthdate DATE NULL,
  ADD COLUMN parent_id BIGINT NULL; -- optional: link to parent user id

CREATE TABLE parental_controls (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  allow_chat BOOLEAN NOT NULL DEFAULT FALSE,
  allow_profile_public BOOLEAN NOT NULL DEFAULT FALSE,
  whitelist JSONB DEFAULT '[]'::jsonb, -- arbitrary whitelist (user ids, content ids)
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Example: add index for parent-child lookups
CREATE INDEX idx_users_parent_id ON users(parent_id);
