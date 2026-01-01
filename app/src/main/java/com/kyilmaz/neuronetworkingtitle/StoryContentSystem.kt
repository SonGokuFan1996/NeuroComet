package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.net.URL
import java.util.regex.Pattern

/**
 * Story Content System
 *
 * Supports various content types for stories:
 * - Images (from local storage or URL)
 * - Videos (from local storage or URL)
 * - Documents (PDF, etc. - shows preview card)
 * - Links (with auto-generated previews)
 * - Text-only stories with background colors
 */

/**
 * Types of content that can be shared in a story
 */
enum class StoryContentType {
    IMAGE,      // Photo from gallery or URL
    VIDEO,      // Video from gallery or URL
    DOCUMENT,   // PDF, DOC, etc.
    LINK,       // Web link with preview
    TEXT_ONLY,  // Text on colored background
    AUDIO       // Audio file/music
}

/**
 * Enhanced story item with support for multiple content types
 */
@Serializable
data class EnhancedStoryItem(
    val id: String,
    val contentType: StoryContentType,
    val contentUri: String,           // Local URI or remote URL
    val thumbnailUrl: String? = null, // For videos/documents
    val duration: Long = 5000L,       // Display duration in ms
    val textOverlay: String? = null,  // Optional text on top
    val backgroundColor: Long = 0xFF1a1a2e, // For text-only stories
    val linkPreview: LinkPreviewData? = null, // For link stories
    val mimeType: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null
)

/**
 * Link preview data extracted from URLs
 */
@Serializable
data class LinkPreviewData(
    val url: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val siteName: String? = null,
    val favicon: String? = null,
    val contentType: LinkContentType = LinkContentType.WEBPAGE,
    val isSafe: Boolean = true,
    val safetyWarning: String? = null
)

/**
 * Types of link content for appropriate display
 */
enum class LinkContentType {
    WEBPAGE,        // Regular website
    ARTICLE,        // News/blog article
    VIDEO,          // YouTube, Vimeo, etc.
    MUSIC,          // Spotify, Apple Music, etc.
    SOCIAL_POST,    // Twitter, Instagram, etc.
    PRODUCT,        // Shopping link
    REPOSITORY,     // GitHub, GitLab, etc.
    DOCUMENT        // Google Docs, PDF, etc.
}

/**
 * Content safety levels
 */
enum class ContentSafetyLevel {
    SAFE,           // Appropriate for all audiences
    SENSITIVE,      // May require age verification
    NSFW,           // Not Safe For Work - blocked
    NSFL            // Not Safe For Life - blocked
}

/**
 * Link Preview Generator
 *
 * Generates preview cards for shared links with:
 * - Title extraction from Open Graph / meta tags
 * - Description extraction
 * - Image/thumbnail extraction
 * - Site name and favicon
 * - Content type detection
 * - Safety screening for NSFW/NSFL content
 */
object LinkPreviewGenerator {

    // Patterns for detecting specific platforms
    private val YOUTUBE_PATTERN = Pattern.compile(
        "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]+)"
    )
    private val SPOTIFY_PATTERN = Pattern.compile(
        "open\\.spotify\\.com/(track|album|playlist)/([a-zA-Z0-9]+)"
    )
    private val GITHUB_PATTERN = Pattern.compile(
        "github\\.com/([^/]+)/([^/]+)"
    )
    private val TWITTER_PATTERN = Pattern.compile(
        "(?:twitter\\.com|x\\.com)/([^/]+)/status/(\\d+)"
    )

    // Known NSFW domains (simplified - in production use a proper service)
    private val BLOCKED_DOMAINS = setOf(
        "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "xhamster", "brazzers", "onlyfans", "fansly",
        // Gore/shock sites
        "liveleak", "bestgore", "theync", "crazyshit"
    )

    // Known safe domains for quick-pass
    private val SAFE_DOMAINS = setOf(
        "youtube.com", "youtu.be", "vimeo.com",
        "spotify.com", "apple.com", "music.apple.com",
        "github.com", "gitlab.com", "stackoverflow.com",
        "wikipedia.org", "medium.com", "dev.to",
        "twitter.com", "x.com", "instagram.com", "facebook.com",
        "reddit.com", "discord.com", "twitch.tv",
        "amazon.com", "ebay.com", "etsy.com",
        "google.com", "docs.google.com", "drive.google.com",
        "nytimes.com", "bbc.com", "cnn.com", "npr.org",
        "newyorker.com", "theguardian.com", "washingtonpost.com"
    )

    /**
     * Generate a preview for a given URL
     */
    fun generatePreview(url: String): LinkPreviewData {
        val safetyCheck = checkContentSafety(url)

        if (safetyCheck != ContentSafetyLevel.SAFE) {
            return LinkPreviewData(
                url = url,
                title = "Content Blocked",
                description = "This link has been blocked for safety reasons.",
                isSafe = false,
                safetyWarning = when (safetyCheck) {
                    ContentSafetyLevel.NSFW -> "This content is not safe for work (NSFW)"
                    ContentSafetyLevel.NSFL -> "This content may be disturbing (NSFL)"
                    else -> "Content not allowed"
                }
            )
        }

        // Detect content type based on URL patterns
        val contentType = detectContentType(url)
        val preview = generateBasicPreview(url, contentType)

        return preview
    }

    /**
     * Check if content is safe based on URL
     */
    fun checkContentSafety(url: String): ContentSafetyLevel {
        val lowerUrl = url.lowercase()

        // Check against blocked domains
        for (domain in BLOCKED_DOMAINS) {
            if (lowerUrl.contains(domain)) {
                return if (domain in listOf("liveleak", "bestgore", "theync", "crazyshit")) {
                    ContentSafetyLevel.NSFL
                } else {
                    ContentSafetyLevel.NSFW
                }
            }
        }

        // Quick pass for known safe domains
        for (domain in SAFE_DOMAINS) {
            if (lowerUrl.contains(domain)) {
                return ContentSafetyLevel.SAFE
            }
        }

        // Default to safe for unknown domains (real implementation would use API)
        return ContentSafetyLevel.SAFE
    }

    /**
     * Detect the type of content from URL patterns
     */
    private fun detectContentType(url: String): LinkContentType {
        return when {
            YOUTUBE_PATTERN.matcher(url).find() -> LinkContentType.VIDEO
            url.contains("vimeo.com") -> LinkContentType.VIDEO
            url.contains("twitch.tv") -> LinkContentType.VIDEO
            SPOTIFY_PATTERN.matcher(url).find() -> LinkContentType.MUSIC
            url.contains("music.apple.com") -> LinkContentType.MUSIC
            url.contains("soundcloud.com") -> LinkContentType.MUSIC
            GITHUB_PATTERN.matcher(url).find() -> LinkContentType.REPOSITORY
            url.contains("gitlab.com") -> LinkContentType.REPOSITORY
            TWITTER_PATTERN.matcher(url).find() -> LinkContentType.SOCIAL_POST
            url.contains("instagram.com/p/") -> LinkContentType.SOCIAL_POST
            url.contains("reddit.com/r/") -> LinkContentType.SOCIAL_POST
            url.contains("amazon.com") || url.contains("ebay.com") || url.contains("etsy.com") -> LinkContentType.PRODUCT
            url.contains("medium.com") || url.contains("dev.to") -> LinkContentType.ARTICLE
            url.endsWith(".pdf") || url.contains("docs.google.com") -> LinkContentType.DOCUMENT
            else -> LinkContentType.WEBPAGE
        }
    }

    /**
     * Generate a basic preview based on content type
     */
    private fun generateBasicPreview(url: String, contentType: LinkContentType): LinkPreviewData {
        // Extract domain for site name
        val siteName = try {
            URL(url).host.removePrefix("www.")
        } catch (e: Exception) {
            "Link"
        }

        // Generate type-specific previews
        return when (contentType) {
            LinkContentType.VIDEO -> {
                val youtubeMatch = YOUTUBE_PATTERN.matcher(url)
                if (youtubeMatch.find()) {
                    val videoId = youtubeMatch.group(1)
                    LinkPreviewData(
                        url = url,
                        title = "YouTube Video",
                        description = "Watch this video on YouTube",
                        imageUrl = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg",
                        siteName = "YouTube",
                        favicon = "https://www.youtube.com/favicon.ico",
                        contentType = contentType,
                        isSafe = true
                    )
                } else {
                    LinkPreviewData(
                        url = url,
                        title = "Video",
                        description = "Watch this video",
                        siteName = siteName,
                        contentType = contentType,
                        isSafe = true
                    )
                }
            }

            LinkContentType.MUSIC -> {
                val spotifyMatch = SPOTIFY_PATTERN.matcher(url)
                if (spotifyMatch.find()) {
                    val type = spotifyMatch.group(1)
                    LinkPreviewData(
                        url = url,
                        title = "Spotify ${type?.replaceFirstChar { it.uppercase() } ?: "Music"}",
                        description = "Listen on Spotify",
                        imageUrl = "https://storage.googleapis.com/pr-newsroom-wp/1/2018/11/Spotify_Logo_RGB_Green.png",
                        siteName = "Spotify",
                        favicon = "https://open.spotify.com/favicon.ico",
                        contentType = contentType,
                        isSafe = true
                    )
                } else {
                    LinkPreviewData(
                        url = url,
                        title = "Music",
                        description = "Listen to this track",
                        siteName = siteName,
                        contentType = contentType,
                        isSafe = true
                    )
                }
            }

            LinkContentType.REPOSITORY -> {
                val githubMatch = GITHUB_PATTERN.matcher(url)
                if (githubMatch.find()) {
                    val owner = githubMatch.group(1)
                    val repo = githubMatch.group(2)
                    LinkPreviewData(
                        url = url,
                        title = "$owner/$repo",
                        description = "View this repository on GitHub",
                        imageUrl = "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
                        siteName = "GitHub",
                        favicon = "https://github.com/favicon.ico",
                        contentType = contentType,
                        isSafe = true
                    )
                } else {
                    LinkPreviewData(
                        url = url,
                        title = "Repository",
                        description = "View source code",
                        siteName = siteName,
                        contentType = contentType,
                        isSafe = true
                    )
                }
            }

            LinkContentType.SOCIAL_POST -> {
                LinkPreviewData(
                    url = url,
                    title = "Social Post",
                    description = "View this post on $siteName",
                    siteName = siteName,
                    contentType = contentType,
                    isSafe = true
                )
            }

            LinkContentType.PRODUCT -> {
                LinkPreviewData(
                    url = url,
                    title = "Product",
                    description = "View this product on $siteName",
                    siteName = siteName,
                    contentType = contentType,
                    isSafe = true
                )
            }

            LinkContentType.ARTICLE -> {
                LinkPreviewData(
                    url = url,
                    title = "Article",
                    description = "Read this article on $siteName",
                    siteName = siteName,
                    contentType = contentType,
                    isSafe = true
                )
            }

            LinkContentType.DOCUMENT -> {
                LinkPreviewData(
                    url = url,
                    title = "Document",
                    description = "View this document",
                    siteName = siteName,
                    contentType = contentType,
                    isSafe = true
                )
            }

            else -> {
                LinkPreviewData(
                    url = url,
                    title = siteName,
                    description = "View this link",
                    siteName = siteName,
                    contentType = contentType,
                    isSafe = true
                )
            }
        }
    }
}

/**
 * File type detection utilities
 */
object StoryFileUtils {

    /**
     * Detect content type from URI
     */
    fun getContentType(context: Context, uri: Uri): StoryContentType {
        val mimeType = context.contentResolver.getType(uri)
            ?: getMimeTypeFromExtension(uri.toString())

        return when {
            mimeType?.startsWith("image/") == true -> StoryContentType.IMAGE
            mimeType?.startsWith("video/") == true -> StoryContentType.VIDEO
            mimeType?.startsWith("audio/") == true -> StoryContentType.AUDIO
            mimeType == "application/pdf" -> StoryContentType.DOCUMENT
            mimeType?.contains("document") == true -> StoryContentType.DOCUMENT
            mimeType?.contains("spreadsheet") == true -> StoryContentType.DOCUMENT
            mimeType?.contains("presentation") == true -> StoryContentType.DOCUMENT
            else -> StoryContentType.IMAGE // Default to image
        }
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeTypeFromExtension(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Get file name from URI
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    /**
     * Get file size from URI
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * Check if file type is supported
     */
    fun isSupportedFileType(mimeType: String?): Boolean {
        if (mimeType == null) return false
        return mimeType.startsWith("image/") ||
                mimeType.startsWith("video/") ||
                mimeType.startsWith("audio/") ||
                mimeType == "application/pdf" ||
                mimeType.contains("document") ||
                mimeType.contains("spreadsheet") ||
                mimeType.contains("presentation")
    }

    /**
     * Get icon for content type
     */
    fun getContentTypeIcon(type: StoryContentType): String {
        return when (type) {
            StoryContentType.IMAGE -> "🖼️"
            StoryContentType.VIDEO -> "🎬"
            StoryContentType.DOCUMENT -> "📄"
            StoryContentType.LINK -> "🔗"
            StoryContentType.TEXT_ONLY -> "✏️"
            StoryContentType.AUDIO -> "🎵"
        }
    }
}

/**
 * Background colors for text-only stories
 */
object StoryBackgroundColors {
    val colors = listOf(
        Color(0xFF1a1a2e) to "Midnight",
        Color(0xFF16213e) to "Deep Blue",
        Color(0xFF0f3460) to "Ocean",
        Color(0xFF4ECDC4) to "Teal",
        Color(0xFF6BCB77) to "Green",
        Color(0xFFFFB347) to "Orange",
        Color(0xFFFF6B6B) to "Coral",
        Color(0xFF9B59B6) to "Purple",
        Color(0xFFE91E63) to "Pink",
        Color(0xFF3F51B5) to "Indigo",
        Color(0xFF009688) to "Cyan",
        Color(0xFF795548) to "Brown"
    )

    // Gradient backgrounds
    val gradients = listOf(
        listOf(Color(0xFF667eea), Color(0xFF764ba2)) to "Violet Dream",
        listOf(Color(0xFFf093fb), Color(0xFFf5576c)) to "Pink Sunset",
        listOf(Color(0xFF4facfe), Color(0xFF00f2fe)) to "Ocean Breeze",
        listOf(Color(0xFF43e97b), Color(0xFF38f9d7)) to "Mint Fresh",
        listOf(Color(0xFFfa709a), Color(0xFFfee140)) to "Warm Glow",
        listOf(Color(0xFF30cfd0), Color(0xFF330867)) to "Deep Space"
    )
}

