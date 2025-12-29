package com.kyilmaz.neuronetworkingtitle

import org.junit.Assert.*
import org.junit.Test

class StoryTest {

    @Test
    fun testStoryCreation() {
        val storyImageUrl = "https://example.com/story.jpg"

        val story = Story(
            id = "1",
            userAvatar = "https://example.com/avatar.jpg",
            userName = "Test User",
            items = listOf(
                StoryItem(
                    id = "item-1",
                    imageUrl = storyImageUrl,
                    duration = 5000L
                )
            )
        )

        assertEquals("1", story.id)
        assertEquals("https://example.com/avatar.jpg", story.userAvatar)
        assertEquals("Test User", story.userName)
        assertEquals(1, story.items.size)
        assertEquals(storyImageUrl, story.items.first().imageUrl)
        assertFalse(story.isViewed)
    }
}