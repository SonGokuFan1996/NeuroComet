package com.kyilmaz.neuronetworkingtitle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long? = null,
    @SerialName("post_id")
    val postId: Long,
    @SerialName("user_id")
    val userId: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_avatar")
    val userAvatar: String? = null
)
