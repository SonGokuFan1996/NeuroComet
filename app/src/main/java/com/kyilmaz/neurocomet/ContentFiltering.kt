package com.kyilmaz.neurocomet

import com.kyilmaz.neurocomet.KidsFilterLevel
import com.kyilmaz.neurocomet.Audience

/**
 * Mock utility for content filtering/sanitization used in FeedScreen.
 */
object ContentFiltering {

    /**
     * Checks if a post can be viewed by the given audience.
     * UNDER_13 can only see UNDER_13 posts.
     * TEEN can see UNDER_13 and TEEN posts.
     * ADULT can see all posts.
     */
    fun canViewPost(postAudience: Audience, userAudience: Audience): Boolean {
        return when (userAudience) {
            Audience.UNDER_13 -> postAudience == Audience.UNDER_13
            Audience.TEEN -> postAudience == Audience.UNDER_13 || postAudience == Audience.TEEN
            Audience.ADULT -> true // Adults can see all content
        }
    }

    /**
     * Filters a list of posts based on the user's audience level.
     */
    fun filterPostsByAudience(posts: List<Post>, userAudience: Audience): List<Post> {
        return posts.filter { canViewPost(it.minAudience, userAudience) }
    }

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
