package com.kyilmaz.neuronetworkingtitle

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val type: NotificationType
)

enum class NotificationType { LIKE, COMMENT, SYSTEM }