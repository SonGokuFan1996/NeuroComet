package com.kyilmaz.neurocomet

data class ModerationHeuristicReport(
    val normalizedContent: String,
    val flaggedMatches: List<String>,
    val blockedMatches: List<String>
) {
    val status: ModerationStatus = when {
        blockedMatches.isNotEmpty() -> ModerationStatus.BLOCKED
        flaggedMatches.isNotEmpty() -> ModerationStatus.FLAGGED
        else -> ModerationStatus.CLEAN
    }
}

object ModerationHeuristics {
    val flaggedKeywords: List<String> = listOf(
        "scam",
        "phishing",
        "hate",
        "link",
        "spam",
        "shit",
        "damn"
    )

    val blockedKeywords: List<String> = listOf(
        "kill",
        "harm",
        "abuse",
        "underage",
        "threat",
        "illegal",
        "criminal"
    )

    fun analyze(content: String): ModerationHeuristicReport {
        val normalizedContent = content.lowercase()
        val blockedMatches = blockedKeywords.filter { normalizedContent.contains(it) }
        val flaggedMatches = flaggedKeywords.filter { normalizedContent.contains(it) }
        return ModerationHeuristicReport(
            normalizedContent = normalizedContent,
            flaggedMatches = flaggedMatches,
            blockedMatches = blockedMatches
        )
    }
}
