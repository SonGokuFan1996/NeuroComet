package com.kyilmaz.neurocomet

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

    private val abuseKeywords = listOf(
        "kill", "harm yourself", "attack you", "hurt you", "threaten",
        "i will find you", "send nudes", "meet me alone", "don't tell your parents",
        "keep this secret", "prove you love me", "you owe me", "i know where you live"
    )
    private val profanityKeywords = listOf("damn", "hell", "crap", "ass")
    private val scamKeywords = listOf(
        "send money", "wire money", "gift card", "crypto", "bitcoin", "investment opportunity",
        "guaranteed return", "bank account", "routing number", "otp code", "verification code",
        "cash app", "venmo", "paypal friends", "telegram me", "whatsapp me", "snap me"
    )
    private val riskyLinkKeywords = listOf("bit.ly", "tinyurl", "t.me", "discord.gg", "grabify", "iplogger")

    private fun normalized(text: String): String =
        text.lowercase().replace(Regex("\\s+"), " ").trim()

    private fun looksLikeScamOrPredatoryText(text: String): Boolean {
        val normalizedText = normalized(text)
        val hasKeyword = scamKeywords.any { normalizedText.contains(it) } ||
            riskyLinkKeywords.any { normalizedText.contains(it) }

        val containsContactExtraction = listOf(
            "phone number", "home address", "school name", "send a pic", "private pic",
            "private photo", "age?", "how old are you", "are you alone"
        ).any { normalizedText.contains(it) }

        val containsUrl = Regex("https?://|www\\.").containsMatchIn(normalizedText)
        return hasKeyword || containsContactExtraction || (containsUrl && normalizedText.contains("dm me"))
    }

    suspend fun analyzeText(text: String): ModerationResult {
        // Simulate network latency for API call
        delay(300) 
        
        val lowerCaseText = normalized(text)

        // 1. Anti-Abuse System (BLOCK)
        if (abuseKeywords.any { lowerCaseText.contains(it) }) {
            return ModerationResult.BLOCKED_ABUSE
        }

        // 2. Profanity System (FLAG - Allowed)
        if (profanityKeywords.any { lowerCaseText.contains(it) }) {
            return ModerationResult.FLAGGED_PROFANITY
        }
        
        // 3. Anti-Scammer / anti-predator heuristics.
        if (looksLikeScamOrPredatoryText(lowerCaseText)) {
             return ModerationResult.BLOCKED_ABUSE
        }

        return ModerationResult.SAFE
    }
}
