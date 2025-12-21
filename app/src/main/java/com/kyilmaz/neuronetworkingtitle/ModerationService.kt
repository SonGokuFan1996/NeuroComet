package com.kyilmaz.neuronetworkingtitle

import kotlinx.coroutines.delay

enum class ModerationResult {
    SAFE,
    FLAGGED_PROFANITY, // Profanity is allowed but flagged
    BLOCKED_ABUSE      // Abuse (mental or physical) is blocked
}

object ModerationService {
    // This is a mock implementation that simulates a call to a remote Moderation API.
    // In a real app, this logic would live on a secure backend and use a service like
    // Google's Perspective API or a dedicated moderation tool.

    private val abuseKeywords = listOf("kill", "harm yourself", "attack you", "hurt you", "threaten")
    private val profanityKeywords = listOf("damn", "hell", "crap", "ass")

    suspend fun analyzeText(text: String): ModerationResult {
        // Simulate network latency for API call
        delay(300) 
        
        val lowerCaseText = text.lowercase()

        // 1. Anti-Abuse System (BLOCK)
        if (abuseKeywords.any { lowerCaseText.contains(it) }) {
            return ModerationResult.BLOCKED_ABUSE
        }

        // 2. Profanity System (FLAG - Allowed)
        if (profanityKeywords.any { lowerCaseText.contains(it) }) {
            return ModerationResult.FLAGGED_PROFANITY
        }
        
        // 3. Anti-Scammer System (Mock Heuristic)
        // Basic heuristic: check for URLs and phrases like "send money"
        if (lowerCaseText.contains("http") || lowerCaseText.contains("send money")) {
             // For a mock, we'll block any post containing these elements, classifying it as abuse.
             // A real system would need complex NLP models for scam detection.
             return ModerationResult.BLOCKED_ABUSE
        }

        return ModerationResult.SAFE
    }
}
