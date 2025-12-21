package com.kyilmaz.neuronetworkingtitle

object MockPostService {
    fun getPosts(): List<Post> {
        return listOf(
            Post(
                id = 1,
                content = "This is a post about something interesting.",
                userId = "user1",
                likes = 10,
                community = "general",
                tone = "neutral",
                imageUrl = "https://picsum.photos/seed/picsum/200/300",
                isLikedByMe = true,
                userAvatar = "https://i.pravatar.cc/150?u=a042581f4e29026704d"
            ),
            Post(
                id = 2,
                content = "This is another post, but this one is much longer and has a lot more to say. It's really important.",
                userId = "user2",
                likes = 25,
                community = "android",
                tone = "positive",
                imageUrl = "https://picsum.photos/seed/picsum/200/300",
                isLikedByMe = false,
                userAvatar = "https://i.pravatar.cc/150?u=a042581f4e29026704e"
            ),
            Post(
                id = 3,
                content = "This is a third post. It's short and sweet.",
                userId = "user3",
                likes = 5,
                community = "random",
                tone = "negative",
                imageUrl = null,
                isLikedByMe = true,
                userAvatar = "https://i.pravatar.cc/150?u=a042581f4e29026704f"
            )
        )
    }
}
