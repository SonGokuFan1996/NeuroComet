@file:Suppress("UnsafeOptInUsageError")

package com.kyilmaz.neurocomet

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Enhanced Story Video Player
 *
 * Features:
 * - No quality degradation - plays original resolution
 * - Full-screen capable
 * - Pause on hold gesture support
 * - Progress tracking for story advancement
 * - Mute/unmute toggle
 * - Lifecycle-aware (pauses when app backgrounded)
 */
@OptIn(UnstableApi::class)
@Composable
fun StoryVideoPlayer(
    videoUri: String,
    isPlaying: Boolean,
    isMuted: Boolean = false,
    onVideoEnded: () -> Unit = {},
    onProgressUpdate: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    showControls: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create ExoPlayer with high-quality settings
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // Set up for maximum quality - no downgrading
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(videoUri))
                    .build()

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = isPlaying
                volume = if (isMuted) 0f else 1f
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    // Track video end
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoEnded()
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Video loaded at full resolution
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Progress tracking
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying && exoPlayer.duration > 0) {
            val progress = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
            onProgressUpdate(progress.coerceIn(0f, 1f))
            delay(100)
        }
    }

    // Control playback state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Control mute state
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) exoPlayer.play()
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Video view
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = showControls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    setKeepContentOnPlayerReset(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        var isBuffering by remember { mutableStateOf(false) }

        LaunchedEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
            }
            exoPlayer.addListener(listener)
        }

        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Video preview thumbnail for story creation
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewThumbnail(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Create player just to get thumbnail frame
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
                playWhenReady = false
                seekTo(1000) // Seek to 1 second for thumbnail
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Play button overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = "Play video",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

/**
 * Inline video player for posts/feed with quality preservation
 */
@OptIn(UnstableApi::class)
@Composable
fun FeedVideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isMuted by remember { mutableStateOf(true) } // Muted by default in feed

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
                prepare()
                playWhenReady = autoPlay
                volume = if (isMuted) 0f else 1f
                repeatMode = Player.REPEAT_MODE_ALL // Loop in feed
            }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = showControls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom mute button overlay
        if (!showControls) {
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    exoPlayer.volume = if (isMuted) 0f else 1f
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(
                    if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = Color.White
                )
            }
        }

        // Tap to play/pause
        if (!showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp), // Leave space for mute button
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    IconButton(
                        onClick = {
                            isPlaying = true
                            exoPlayer.play()
                        }
                    ) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Media quality settings - ensures no degradation
 */
object MediaQualitySettings {
    /**
     * Coil image loading configuration for maximum quality
     * Use these settings when loading images to prevent quality loss
     */
    const val DISABLE_HARDWARE_BITMAPS = false // Hardware bitmaps are fine for display
    const val ALLOW_RGB_565 = false // Use ARGB_8888 for full quality

    /**
     * Maximum dimensions - don't resize images
     * Setting to Int.MAX_VALUE means no resizing
     */
    const val MAX_IMAGE_WIDTH = Int.MAX_VALUE
    const val MAX_IMAGE_HEIGHT = Int.MAX_VALUE

    /**
     * Video quality settings
     */
    const val PREFER_HIGHEST_QUALITY = true
    const val ENABLE_HARDWARE_ACCELERATION = true
}

