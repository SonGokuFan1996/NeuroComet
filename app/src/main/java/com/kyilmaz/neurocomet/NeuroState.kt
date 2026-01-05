package com.kyilmaz.neurocomet

import android.content.Context
import androidx.annotation.StringRes

/**
 * Neurodivergent-friendly theming states that adapt the UI based on:
 * - Condition/Disorder type
 * - Current mood/energy level
 * - Sensory needs
 */
enum class NeuroState(
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int,
    val emoji: String
) {
    // Basic states
    DEFAULT(R.string.neuro_state_default, R.string.neuro_state_default_desc, "🎨"),
    HYPERFOCUS(R.string.neuro_state_hyperfocus, R.string.neuro_state_hyperfocus_desc, "🎯"),
    OVERLOAD(R.string.neuro_state_overload, R.string.neuro_state_overload_desc, "🌊"),
    CALM(R.string.neuro_state_calm, R.string.neuro_state_calm_desc, "🌿"),

    // ADHD-focused states
    ADHD_ENERGIZED(R.string.neuro_state_adhd_energized, R.string.neuro_state_adhd_energized_desc, "⚡"),
    ADHD_LOW_DOPAMINE(R.string.neuro_state_adhd_low_dopamine, R.string.neuro_state_adhd_low_dopamine_desc, "🌅"),
    ADHD_TASK_MODE(R.string.neuro_state_adhd_task_mode, R.string.neuro_state_adhd_task_mode_desc, "📋"),

    // Autism-focused states
    AUTISM_ROUTINE(R.string.neuro_state_autism_routine, R.string.neuro_state_autism_routine_desc, "🔄"),
    AUTISM_SENSORY_SEEK(R.string.neuro_state_autism_sensory_seek, R.string.neuro_state_autism_sensory_seek_desc, "✨"),
    AUTISM_LOW_STIM(R.string.neuro_state_autism_low_stim, R.string.neuro_state_autism_low_stim_desc, "🤫"),

    // Anxiety/OCD-focused states
    ANXIETY_SOOTHE(R.string.neuro_state_anxiety_soothe, R.string.neuro_state_anxiety_soothe_desc, "💙"),
    ANXIETY_GROUNDING(R.string.neuro_state_anxiety_grounding, R.string.neuro_state_anxiety_grounding_desc, "🌍"),

    // Dyslexia-focused states
    DYSLEXIA_FRIENDLY(R.string.neuro_state_dyslexia_friendly, R.string.neuro_state_dyslexia_friendly_desc, "📖"),

    // Colorblind-friendly states
    COLORBLIND_DEUTERANOPIA(R.string.neuro_state_colorblind_deuter, R.string.neuro_state_colorblind_deuter_desc, "👁️"),
    COLORBLIND_PROTANOPIA(R.string.neuro_state_colorblind_protan, R.string.neuro_state_colorblind_protan_desc, "👁️"),
    COLORBLIND_TRITANOPIA(R.string.neuro_state_colorblind_tritan, R.string.neuro_state_colorblind_tritan_desc, "👁️"),
    COLORBLIND_MONOCHROMACY(R.string.neuro_state_colorblind_mono, R.string.neuro_state_colorblind_mono_desc, "⚫"),

    // Blind/Screen Reader accessibility states
    BLIND_SCREEN_READER(R.string.neuro_state_blind_screen_reader, R.string.neuro_state_blind_screen_reader_desc, "🔊"),
    BLIND_HIGH_CONTRAST(R.string.neuro_state_blind_high_contrast, R.string.neuro_state_blind_high_contrast_desc, "◐"),
    BLIND_LARGE_TEXT(R.string.neuro_state_blind_large_text, R.string.neuro_state_blind_large_text_desc, "🔤"),

    // Energy/Mood-based states
    MOOD_TIRED(R.string.neuro_state_mood_tired, R.string.neuro_state_mood_tired_desc, "😴"),
    MOOD_ANXIOUS(R.string.neuro_state_mood_anxious, R.string.neuro_state_mood_anxious_desc, "🫂"),
    MOOD_HAPPY(R.string.neuro_state_mood_happy, R.string.neuro_state_mood_happy_desc, "😊"),
    MOOD_OVERWHELMED(R.string.neuro_state_mood_overwhelmed, R.string.neuro_state_mood_overwhelmed_desc, "🧘"),
    MOOD_CREATIVE(R.string.neuro_state_mood_creative, R.string.neuro_state_mood_creative_desc, "🎨"),

    // Secret unlockable theme - Rainbow Brain!
    RAINBOW_BRAIN(R.string.neuro_state_rainbow_brain, R.string.neuro_state_rainbow_brain_desc, "🦄");

    /**
     * Get the display name string using context
     */
    fun getDisplayName(context: Context): String = context.getString(displayNameResId)

    /**
     * Get the description string using context
     */
    fun getDescription(context: Context): String = context.getString(descriptionResId)
}

/**
 * Categorized groups for easier UI navigation
 */
enum class NeuroStateCategory(
    @StringRes val displayNameResId: Int,
    val states: List<NeuroState>,
    val isSecret: Boolean = false
) {
    BASIC(R.string.neuro_category_basic, listOf(NeuroState.DEFAULT, NeuroState.HYPERFOCUS, NeuroState.OVERLOAD, NeuroState.CALM)),
    ADHD(R.string.neuro_category_adhd, listOf(NeuroState.ADHD_ENERGIZED, NeuroState.ADHD_LOW_DOPAMINE, NeuroState.ADHD_TASK_MODE)),
    AUTISM(R.string.neuro_category_autism, listOf(NeuroState.AUTISM_ROUTINE, NeuroState.AUTISM_SENSORY_SEEK, NeuroState.AUTISM_LOW_STIM)),
    ANXIETY(R.string.neuro_category_anxiety, listOf(NeuroState.ANXIETY_SOOTHE, NeuroState.ANXIETY_GROUNDING)),
    ACCESSIBILITY(R.string.neuro_category_accessibility, listOf(NeuroState.DYSLEXIA_FRIENDLY)),
    COLORBLIND(R.string.neuro_category_colorblind, listOf(NeuroState.COLORBLIND_DEUTERANOPIA, NeuroState.COLORBLIND_PROTANOPIA, NeuroState.COLORBLIND_TRITANOPIA, NeuroState.COLORBLIND_MONOCHROMACY)),
    BLIND(R.string.neuro_category_blind, listOf(NeuroState.BLIND_SCREEN_READER, NeuroState.BLIND_HIGH_CONTRAST, NeuroState.BLIND_LARGE_TEXT)),
    MOOD(R.string.neuro_category_mood, listOf(NeuroState.MOOD_TIRED, NeuroState.MOOD_ANXIOUS, NeuroState.MOOD_HAPPY, NeuroState.MOOD_OVERWHELMED, NeuroState.MOOD_CREATIVE)),
    SECRET(R.string.neuro_category_secret, listOf(NeuroState.RAINBOW_BRAIN), isSecret = true);

    /**
     * Get the display name string using context
     */
    fun getDisplayName(context: Context): String = context.getString(displayNameResId)
}
