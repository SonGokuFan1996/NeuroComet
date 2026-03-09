-- ============================================================================
-- CALLING TABLES: call_signals (WebRTC signaling) & call_history
-- ============================================================================

-- Call signals table for WebRTC signaling via Supabase Realtime
CREATE TABLE IF NOT EXISTS call_signals (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    call_id TEXT NOT NULL,
    from_user_id TEXT NOT NULL,
    to_user_id TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('offer', 'answer', 'ice_candidate', 'call_request', 'call_accept', 'call_decline', 'call_end')),
    payload TEXT DEFAULT '',
    call_type TEXT DEFAULT 'VOICE' CHECK (call_type IN ('VOICE', 'VIDEO')),
    caller_name TEXT DEFAULT 'Unknown',
    caller_avatar TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Index for fast lookups by recipient (incoming signals)
CREATE INDEX IF NOT EXISTS idx_call_signals_to_user ON call_signals (to_user_id, created_at DESC);
-- Index for cleanup of stale signals
CREATE INDEX IF NOT EXISTS idx_call_signals_created_at ON call_signals (created_at);

-- RLS policies
ALTER TABLE call_signals ENABLE ROW LEVEL SECURITY;

-- Users can insert signals (to make calls)
CREATE POLICY "Users can insert call signals"
    ON call_signals FOR INSERT
    WITH CHECK (auth.uid()::text = from_user_id);

-- Users can read signals addressed to them
CREATE POLICY "Users can read their own call signals"
    ON call_signals FOR SELECT
    USING (auth.uid()::text = to_user_id OR auth.uid()::text = from_user_id);

-- Auto-delete stale signals older than 5 minutes (cleanup function)
CREATE OR REPLACE FUNCTION cleanup_stale_call_signals()
RETURNS void AS $$
BEGIN
    DELETE FROM call_signals WHERE created_at < now() - INTERVAL '5 minutes';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable Realtime for call_signals so clients receive INSERT events
ALTER PUBLICATION supabase_realtime ADD TABLE call_signals;

-- ============================================================================
-- Call history table for persistent call logs
-- ============================================================================

CREATE TABLE IF NOT EXISTS call_history (
    id TEXT PRIMARY KEY DEFAULT 'call_' || extract(epoch from now())::bigint::text || '_' || gen_random_uuid()::text,
    caller_id TEXT NOT NULL,
    recipient_id TEXT NOT NULL,
    recipient_name TEXT DEFAULT 'Unknown',
    recipient_avatar TEXT DEFAULT '',
    call_type TEXT DEFAULT 'VOICE' CHECK (call_type IN ('VOICE', 'VIDEO')),
    is_outgoing BOOLEAN DEFAULT true,
    outcome TEXT DEFAULT 'COMPLETED' CHECK (outcome IN ('COMPLETED', 'MISSED', 'DECLINED', 'NO_ANSWER', 'CANCELLED', 'FAILED')),
    duration_seconds BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Index for fetching user's call history
CREATE INDEX IF NOT EXISTS idx_call_history_caller ON call_history (caller_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_call_history_recipient ON call_history (recipient_id, created_at DESC);

-- RLS policies
ALTER TABLE call_history ENABLE ROW LEVEL SECURITY;

-- Users can insert their own call history
CREATE POLICY "Users can insert their call history"
    ON call_history FOR INSERT
    WITH CHECK (auth.uid()::text = caller_id);

-- Users can read calls they participated in
CREATE POLICY "Users can read their call history"
    ON call_history FOR SELECT
    USING (auth.uid()::text = caller_id OR auth.uid()::text = recipient_id);

-- Users can delete their own call history
CREATE POLICY "Users can delete their call history"
    ON call_history FOR DELETE
    USING (auth.uid()::text = caller_id);

