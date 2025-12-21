package com.kyilmaz.neuronetworkingtitle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Post(
    val id: Long? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    val content: String,

    @SerialName("user_id")
    val userId: String? = null,

    val likes: Int = 0,

    val community: String? = null,

    val tone: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    // ADDED: Missing fields for PostCard and MockPostService
    @SerialName("video_url")
    val videoUrl: String? = null,

    @SerialName("user_avatar")
    val userAvatar: String? = null,

    // CRITICAL FIX: @Transient ensures this UI-only field isn't sent to Supabase
    @Transient
    val isLikedByMe: Boolean = false
)