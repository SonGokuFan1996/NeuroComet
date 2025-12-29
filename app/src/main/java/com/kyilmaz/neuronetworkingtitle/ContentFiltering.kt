package com.kyilmaz.neuronetworkingtitle

import com.kyilmaz.neuronetworkingtitle.KidsFilterLevel

/**
 * Mock utility for content filtering/sanitization used in FeedScreen.
 */
object ContentFiltering {
    fun shouldHideTextForKids(content: String, level: KidsFilterLevel): Boolean {
        // Mock logic: hide if content contains violence or is too short (low effort post)
        if (content.length < 5) return true
        if (level == KidsFilterLevel.STRICT && content.contains("violence", ignoreCase = true)) return true
        return false
    }

    fun sanitizeForKids(content: String, level: KidsFilterLevel): String {
        return when (level) {
            KidsFilterLevel.STRICT -> content.replace("bad word", "good word", ignoreCase = true) // Mock replacement
            KidsFilterLevel.MODERATE -> content
        }
    }
}