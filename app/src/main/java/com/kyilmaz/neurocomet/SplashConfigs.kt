package com.kyilmaz.neurocomet

import android.content.Context
import androidx.annotation.StringRes

/**
 * Splash screen configurations for different neurodivergent states.
 * Each configuration contains messages and visual style tailored to specific needs.
 */
data class SplashConfig(
    @StringRes val messageResIds: List<Int>,
    @StringRes val taglineResId: Int,
    val animationStyle: SplashAnimationStyle,
    val durationMs: Long = 2000L
) {
    /**
     * Get resolved messages using context
     */
    fun getMessages(context: Context): List<String> {
        return messageResIds.map { context.getString(it) }
    }

    /**
     * Get resolved tagline using context
     */
    fun getTagline(context: Context): String {
        return context.getString(taglineResId)
    }
}

enum class SplashAnimationStyle {
    CALM_WAVES,      // Gentle flowing waves - for anxiety/overwhelm
    FOCUS_PULSE,     // Centered pulsing - for ADHD focus modes
    ROUTINE_GRID,    // Predictable patterns - for autism routine
    ENERGY_BURST,    // Vibrant expanding - for energized states
    GROUNDING_EARTH, // Stable, earthy - for grounding/anxiety
    CREATIVE_SWIRL,  // Inspiring spirals - for creative moods
    GENTLE_FLOAT,    // Soft floating - for tired/low energy
    SENSORY_SPARKLE, // Satisfying particles - for sensory seeking
    CONTRAST_RINGS,  // High contrast concentric rings - for colorblind modes
    PATTERN_SHAPES,  // Distinct geometric patterns - for monochromacy
    RAINBOW_SPARKLE  // Magical rainbow celebration - for secret Rainbow Brain theme
}

/**
 * Get the appropriate splash configuration based on the user's selected NeuroState.
 */
fun getSplashConfigForState(state: NeuroState): SplashConfig {
    return when (state) {
        // Basic states
        NeuroState.DEFAULT -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_default_1, R.string.splash_msg_default_2, R.string.splash_msg_default_3),
            taglineResId = R.string.splash_tagline_default,
            animationStyle = SplashAnimationStyle.CALM_WAVES
        )
        NeuroState.HYPERFOCUS -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_hyperfocus_1, R.string.splash_msg_hyperfocus_2, R.string.splash_msg_hyperfocus_3),
            taglineResId = R.string.splash_tagline_hyperfocus,
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 1500L
        )
        NeuroState.OVERLOAD -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_overload_1, R.string.splash_msg_overload_2, R.string.splash_msg_overload_3),
            taglineResId = R.string.splash_tagline_overload,
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.CALM -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_calm_1, R.string.splash_msg_calm_2, R.string.splash_msg_calm_3),
            taglineResId = R.string.splash_tagline_calm,
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT
        )

        // ADHD states
        NeuroState.ADHD_ENERGIZED -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_adhd_energized_1, R.string.splash_msg_adhd_energized_2, R.string.splash_msg_adhd_energized_3),
            taglineResId = R.string.splash_tagline_adhd_energized,
            animationStyle = SplashAnimationStyle.ENERGY_BURST,
            durationMs = 1500L
        )
        NeuroState.ADHD_LOW_DOPAMINE -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_adhd_low_dopamine_1, R.string.splash_msg_adhd_low_dopamine_2, R.string.splash_msg_adhd_low_dopamine_3),
            taglineResId = R.string.splash_tagline_adhd_low_dopamine,
            animationStyle = SplashAnimationStyle.CREATIVE_SWIRL
        )
        NeuroState.ADHD_TASK_MODE -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_adhd_task_mode_1, R.string.splash_msg_adhd_task_mode_2, R.string.splash_msg_adhd_task_mode_3),
            taglineResId = R.string.splash_tagline_adhd_task_mode,
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 1200L
        )

        // Autism states
        NeuroState.AUTISM_ROUTINE -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_autism_routine_1, R.string.splash_msg_autism_routine_2, R.string.splash_msg_autism_routine_3),
            taglineResId = R.string.splash_tagline_autism_routine,
            animationStyle = SplashAnimationStyle.ROUTINE_GRID
        )
        NeuroState.AUTISM_SENSORY_SEEK -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_autism_sensory_seek_1, R.string.splash_msg_autism_sensory_seek_2, R.string.splash_msg_autism_sensory_seek_3),
            taglineResId = R.string.splash_tagline_autism_sensory_seek,
            animationStyle = SplashAnimationStyle.SENSORY_SPARKLE
        )
        NeuroState.AUTISM_LOW_STIM -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_autism_low_stim_1, R.string.splash_msg_autism_low_stim_2, R.string.splash_msg_autism_low_stim_3),
            taglineResId = R.string.splash_tagline_autism_low_stim,
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT,
            durationMs = 2500L
        )

        // Anxiety states
        NeuroState.ANXIETY_SOOTHE -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_anxiety_soothe_1, R.string.splash_msg_anxiety_soothe_2, R.string.splash_msg_anxiety_soothe_3),
            taglineResId = R.string.splash_tagline_anxiety_soothe,
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.ANXIETY_GROUNDING -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_anxiety_grounding_1, R.string.splash_msg_anxiety_grounding_2, R.string.splash_msg_anxiety_grounding_3),
            taglineResId = R.string.splash_tagline_anxiety_grounding,
            animationStyle = SplashAnimationStyle.GROUNDING_EARTH
        )

        // Accessibility
        NeuroState.DYSLEXIA_FRIENDLY -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_dyslexia_friendly_1, R.string.splash_msg_dyslexia_friendly_2, R.string.splash_msg_dyslexia_friendly_3),
            taglineResId = R.string.splash_tagline_dyslexia_friendly,
            animationStyle = SplashAnimationStyle.FOCUS_PULSE
        )

        // Colorblind themes
        NeuroState.COLORBLIND_DEUTERANOPIA -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_colorblind_deuter_1, R.string.splash_msg_colorblind_deuter_2, R.string.splash_msg_colorblind_deuter_3),
            taglineResId = R.string.splash_tagline_colorblind_deuteranopia,
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_PROTANOPIA -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_colorblind_protan_1, R.string.splash_msg_colorblind_protan_2, R.string.splash_msg_colorblind_protan_3),
            taglineResId = R.string.splash_tagline_colorblind_protanopia,
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_TRITANOPIA -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_colorblind_tritan_1, R.string.splash_msg_colorblind_tritan_2, R.string.splash_msg_colorblind_tritan_3),
            taglineResId = R.string.splash_tagline_colorblind_tritanopia,
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_MONOCHROMACY -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_colorblind_mono_1, R.string.splash_msg_colorblind_mono_2, R.string.splash_msg_colorblind_mono_3),
            taglineResId = R.string.splash_tagline_colorblind_monochromacy,
            animationStyle = SplashAnimationStyle.PATTERN_SHAPES
        )

        // Blind / Low Vision themes
        NeuroState.BLIND_SCREEN_READER -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_blind_reader_1, R.string.splash_msg_blind_reader_2, R.string.splash_msg_blind_reader_3),
            taglineResId = R.string.splash_tagline_blind_screen_reader,
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 2500L // Longer to allow screen reader to announce
        )
        NeuroState.BLIND_HIGH_CONTRAST -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_blind_contrast_1, R.string.splash_msg_blind_contrast_2, R.string.splash_msg_blind_contrast_3),
            taglineResId = R.string.splash_tagline_blind_high_contrast,
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.BLIND_LARGE_TEXT -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_blind_large_1, R.string.splash_msg_blind_large_2, R.string.splash_msg_blind_large_3),
            taglineResId = R.string.splash_tagline_blind_large_text,
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 2500L
        )

        // Mood states
        NeuroState.MOOD_TIRED -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_mood_tired_1, R.string.splash_msg_mood_tired_2, R.string.splash_msg_mood_tired_3),
            taglineResId = R.string.splash_tagline_mood_tired,
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT,
            durationMs = 2500L
        )
        NeuroState.MOOD_ANXIOUS -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_mood_anxious_1, R.string.splash_msg_mood_anxious_2, R.string.splash_msg_mood_anxious_3),
            taglineResId = R.string.splash_tagline_mood_anxious,
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.MOOD_HAPPY -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_mood_happy_1, R.string.splash_msg_mood_happy_2, R.string.splash_msg_mood_happy_3),
            taglineResId = R.string.splash_tagline_mood_happy,
            animationStyle = SplashAnimationStyle.ENERGY_BURST,
            durationMs = 1500L
        )
        NeuroState.MOOD_OVERWHELMED -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_mood_overwhelmed_1, R.string.splash_msg_mood_overwhelmed_2, R.string.splash_msg_mood_overwhelmed_3),
            taglineResId = R.string.splash_tagline_mood_overwhelmed,
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 3000L
        )
        NeuroState.MOOD_CREATIVE -> SplashConfig(
            messageResIds = listOf(R.string.splash_msg_mood_creative_1, R.string.splash_msg_mood_creative_2, R.string.splash_msg_mood_creative_3),
            taglineResId = R.string.splash_tagline_mood_creative,
            animationStyle = SplashAnimationStyle.CREATIVE_SWIRL
        )

        // Secret Theme
        NeuroState.RAINBOW_BRAIN -> SplashConfig(
            messageResIds = listOf(
                R.string.splash_msg_rainbow_1,
                R.string.splash_msg_rainbow_2,
                R.string.splash_msg_rainbow_3,
                R.string.splash_msg_rainbow_4,
                R.string.splash_msg_rainbow_5
            ),
            taglineResId = R.string.splash_tagline_rainbow_brain,
            animationStyle = SplashAnimationStyle.RAINBOW_SPARKLE,
            durationMs = 2000L
        )
    }
}
