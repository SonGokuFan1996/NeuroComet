-- Fix: Make is_liked_by_me nullable with a default value of false.
-- This column is architecturally flawed (per-user state on a global table)
-- but we keep it for backward compatibility while making it NOT crash on insert.
--
-- The 23502 error "null value in column is_liked_by_me violates not-null constraint"
-- occurred because inserts didn't always include this field.

ALTER TABLE posts
  ALTER COLUMN is_liked_by_me SET DEFAULT false;

-- If the column was NOT NULL without a default, this fixes inserts that omit it.
-- For a proper fix, is_liked_by_me should be computed per-user via a view or
-- a join with post_likes, rather than stored on the posts table.

