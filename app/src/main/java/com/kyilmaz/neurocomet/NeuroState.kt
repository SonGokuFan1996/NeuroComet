package com.kyilmaz.neurocomet

/**
 * Neurodivergent-friendly theming states that adapt the UI based on:
 * - Condition/Disorder type
 * - Current mood/energy level
 * - Sensory needs
 */
enum class NeuroState(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    // Basic states
    DEFAULT("Default", "Standard theme with balanced colors", "🎨"),
    HYPERFOCUS("Hyperfocus", "High contrast for deep concentration", "🎯"),
    OVERLOAD("Sensory Overload", "Muted, calm colors to reduce stimulation", "🌊"),
    CALM("Calm", "Soft, soothing colors for relaxation", "🌿"),

    // ADHD-focused states
    ADHD_ENERGIZED("ADHD - Energized", "Bright, engaging colors for productive days", "⚡"),
    ADHD_LOW_DOPAMINE("ADHD - Low Dopamine", "Warm, stimulating colors to boost mood", "🌅"),
    ADHD_TASK_MODE("ADHD - Task Mode", "Minimal distractions, focus-enhancing palette", "📋"),

    // Autism-focused states
    AUTISM_ROUTINE("Autism - Routine", "Predictable, consistent color patterns", "🔄"),
    AUTISM_SENSORY_SEEK("Autism - Sensory Seeking", "Rich textures and satisfying contrasts", "✨"),
    AUTISM_LOW_STIM("Autism - Low Stimulation", "Very muted, gentle colors", "🤫"),

    // Anxiety/OCD-focused states
    ANXIETY_SOOTHE("Anxiety - Soothe", "Cool, reassuring colors to ease worry", "💙"),
    ANXIETY_GROUNDING("Anxiety - Grounding", "Earthy, stable colors for centering", "🌍"),

    // Dyslexia-focused states
    DYSLEXIA_FRIENDLY("Dyslexia Friendly", "High readability with optimal contrast", "📖"),

    // Colorblind-friendly states
    COLORBLIND_DEUTERANOPIA("Deuteranopia", "Optimized for red-green (green-weak) color blindness", "👁️"),
    COLORBLIND_PROTANOPIA("Protanopia", "Optimized for red-green (red-weak) color blindness", "👁️"),
    COLORBLIND_TRITANOPIA("Tritanopia", "Optimized for blue-yellow color blindness", "👁️"),
    COLORBLIND_MONOCHROMACY("Monochromacy", "High contrast grayscale for complete color blindness", "⚫"),

    // Blind/Screen Reader accessibility states
    BLIND_SCREEN_READER("Screen Reader Mode", "Optimized for TalkBack and screen readers with maximum contrast", "🔊"),
    BLIND_HIGH_CONTRAST("Maximum Contrast", "Pure black/white for users with very low vision", "◐"),
    BLIND_LARGE_TEXT("Large Text Mode", "Extra large text with simplified layout", "🔤"),

    // Energy/Mood-based states
    MOOD_TIRED("Feeling Tired", "Gentle colors that don't strain the eyes", "😴"),
    MOOD_ANXIOUS("Feeling Anxious", "Calming palette to reduce stress", "🫂"),
    MOOD_HAPPY("Feeling Happy", "Cheerful colors to match your mood", "😊"),
    MOOD_OVERWHELMED("Feeling Overwhelmed", "Simplified, quiet palette", "🧘"),
    MOOD_CREATIVE("Feeling Creative", "Inspiring colors to fuel imagination", "🎨"),

    // Secret unlockable theme - Rainbow Brain!
    RAINBOW_BRAIN("Rainbow Brain", "Celebrate your beautifully unique neurodivergent mind! 🌈🧠", "🦄")
}

/**
 * Categorized groups for easier UI navigation
 */
enum class NeuroStateCategory(val displayName: String, val states: List<NeuroState>, val isSecret: Boolean = false) {
    BASIC("Basic Themes", listOf(NeuroState.DEFAULT, NeuroState.HYPERFOCUS, NeuroState.OVERLOAD, NeuroState.CALM)),
    ADHD("ADHD Themes", listOf(NeuroState.ADHD_ENERGIZED, NeuroState.ADHD_LOW_DOPAMINE, NeuroState.ADHD_TASK_MODE)),
    AUTISM("Autism Themes", listOf(NeuroState.AUTISM_ROUTINE, NeuroState.AUTISM_SENSORY_SEEK, NeuroState.AUTISM_LOW_STIM)),
    ANXIETY("Anxiety/OCD Themes", listOf(NeuroState.ANXIETY_SOOTHE, NeuroState.ANXIETY_GROUNDING)),
    ACCESSIBILITY("Accessibility", listOf(NeuroState.DYSLEXIA_FRIENDLY)),
    COLORBLIND("Colorblind Friendly", listOf(NeuroState.COLORBLIND_DEUTERANOPIA, NeuroState.COLORBLIND_PROTANOPIA, NeuroState.COLORBLIND_TRITANOPIA, NeuroState.COLORBLIND_MONOCHROMACY)),
    BLIND("Blind & Low Vision", listOf(NeuroState.BLIND_SCREEN_READER, NeuroState.BLIND_HIGH_CONTRAST, NeuroState.BLIND_LARGE_TEXT)),
    MOOD("How Are You Feeling?", listOf(NeuroState.MOOD_TIRED, NeuroState.MOOD_ANXIOUS, NeuroState.MOOD_HAPPY, NeuroState.MOOD_OVERWHELMED, NeuroState.MOOD_CREATIVE)),
    SECRET("🦄 Secret Themes", listOf(NeuroState.RAINBOW_BRAIN), isSecret = true)
}
