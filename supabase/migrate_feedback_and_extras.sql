-- ============================================================================
-- NeuroComet: Missing Feedback, Games, AI Logs, and Premium Status
-- Run this in the Supabase Dashboard SQL Editor
-- ============================================================================

-- 1. Feedback Table (Required for FeedbackSystem.kt)
CREATE TABLE IF NOT EXISTS public.feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    type TEXT NOT NULL, -- bug_report, feature_request, general_feedback
    title TEXT,
    description TEXT NOT NULL,
    severity TEXT, -- low, medium, high, critical
    category TEXT, -- accessibility, social, safety, etc.
    rating INTEGER, -- 1-5 for general feedback
    device_info TEXT,
    app_version TEXT,
    status TEXT NOT NULL DEFAULT 'submitted', -- submitted, under_review, planned, completed, declined
    votes INTEGER NOT NULL DEFAULT 0,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_feedback_type ON public.feedback(type);
CREATE INDEX IF NOT EXISTS idx_feedback_status ON public.feedback(status);
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON public.feedback(user_id);

-- 2. Game Achievements (For syncing game progress)
CREATE TABLE IF NOT EXISTS public.user_game_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    achievement_count INTEGER NOT NULL DEFAULT 0,
    unlocked_game_ids TEXT[], -- Array of strings (game IDs)
    last_played_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id)
);

CREATE INDEX IF NOT EXISTS idx_game_achievements_user ON public.user_game_achievements(user_id);

-- 3. AI Practice Call Logs
CREATE TABLE IF NOT EXISTS public.practice_call_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    persona_id TEXT NOT NULL, -- Alex, Jordan, Sam, etc.
    duration_seconds INTEGER NOT NULL DEFAULT 0,
    message_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_practice_calls_user ON public.practice_call_logs(user_id);

-- 4. User Preferences / Settings Sync
CREATE TABLE IF NOT EXISTS public.user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    theme_mode TEXT DEFAULT 'system',
    is_high_contrast BOOLEAN DEFAULT false,
    reduce_motion BOOLEAN DEFAULT false,
    dyslexia_font BOOLEAN DEFAULT false,
    text_scale_factor FLOAT DEFAULT 1.0,
    notifications_enabled BOOLEAN DEFAULT true,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id)
);

-- 5. AB Test Assignments (Consistency across devices)
CREATE TABLE IF NOT EXISTS public.ab_test_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    experiment_id TEXT NOT NULL,
    variant_id TEXT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, experiment_id)
);

-- 6. Add Premium Columns to Profiles and Users
DO $$ BEGIN
  ALTER TABLE public.users ADD COLUMN IF NOT EXISTS is_premium BOOLEAN DEFAULT false;
  ALTER TABLE public.users ADD COLUMN IF NOT EXISTS premium_until TIMESTAMPTZ;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS is_premium BOOLEAN DEFAULT false;
  ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS premium_until TIMESTAMPTZ;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;

-- ============================================================================
-- RLS POLICIES
-- ============================================================================

ALTER TABLE public.feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_game_achievements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.practice_call_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ab_test_assignments ENABLE ROW LEVEL SECURITY;

-- Feedback: Anyone can insert, users can view own
DROP POLICY IF EXISTS "Anyone can create feedback" ON public.feedback;
CREATE POLICY "Anyone can create feedback" ON public.feedback FOR INSERT WITH CHECK (true);
DROP POLICY IF EXISTS "Users can view own feedback" ON public.feedback;
CREATE POLICY "Users can view own feedback" ON public.feedback FOR SELECT USING (auth.uid() = user_id);

-- Game Achievements: Users can manage own
DROP POLICY IF EXISTS "Users can manage own game achievements" ON public.user_game_achievements;
CREATE POLICY "Users can manage own game achievements" ON public.user_game_achievements
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- Practice Call Logs: Users can manage own
DROP POLICY IF EXISTS "Users can view own practice logs" ON public.practice_call_logs;
CREATE POLICY "Users can view own practice logs" ON public.practice_call_logs
    FOR SELECT USING (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can insert own practice logs" ON public.practice_call_logs;
CREATE POLICY "Users can insert own practice logs" ON public.practice_call_logs
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Preferences: Users can manage own
DROP POLICY IF EXISTS "Users can manage own preferences" ON public.user_preferences;
CREATE POLICY "Users can manage own preferences" ON public.user_preferences
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- AB Test: Users can manage own assignments (needed for client-side upsert)
DROP POLICY IF EXISTS "Users can view own assignments" ON public.ab_test_assignments;
CREATE POLICY "Users can view own assignments" ON public.ab_test_assignments
    FOR SELECT USING (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can insert own assignments" ON public.ab_test_assignments;
CREATE POLICY "Users can insert own assignments" ON public.ab_test_assignments
    FOR INSERT WITH CHECK (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can update own assignments" ON public.ab_test_assignments;
CREATE POLICY "Users can update own assignments" ON public.ab_test_assignments
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- ============================================================================
-- GRANTS
-- ============================================================================

GRANT SELECT, INSERT, UPDATE ON public.feedback TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.user_game_achievements TO authenticated;
GRANT SELECT, INSERT ON public.practice_call_logs TO authenticated;
GRANT SELECT, INSERT, UPDATE ON public.user_preferences TO authenticated;
GRANT SELECT, INSERT, UPDATE ON public.ab_test_assignments TO authenticated;

-- ============================================================================
-- DONE!
-- ============================================================================
