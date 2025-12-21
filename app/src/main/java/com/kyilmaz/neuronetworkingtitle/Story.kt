package com.kyilmaz.neuronetworkingtitle

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String,
    val userId: String,
    val userAvatarUrl: String,
    val imageUrl: String,
    val isViewed: Boolean = false
)
