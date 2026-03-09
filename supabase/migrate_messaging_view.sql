-- Create a view to efficiently get conversations with latest message info
CREATE OR REPLACE VIEW public.vw_conversations_summary AS
WITH LatestMessages AS (
    SELECT DISTINCT ON (conversation_id)
        conversation_id,
        content,
        created_at as last_message_at,
        sender_id,
        is_read
    FROM public.dm_messages
    ORDER BY conversation_id, created_at DESC
),
UnreadCounts AS (
    SELECT
        conversation_id,
        COUNT(*) as unread_count
    FROM public.dm_messages
    WHERE is_read = false
    GROUP BY conversation_id
)
SELECT
    c.id as conversation_id,
    c.is_group,
    c.group_name,
    lm.content as last_message,
    lm.last_message_at,
    lm.sender_id as last_message_sender_id,
    COALESCE(uc.unread_count, 0) as unread_count,
    c.updated_at
FROM public.conversations c
LEFT JOIN LatestMessages lm ON c.id = lm.conversation_id
LEFT JOIN UnreadCounts uc ON c.id = uc.conversation_id;

-- Grant select permission to authenticated users
GRANT SELECT ON public.vw_conversations_summary TO authenticated;
