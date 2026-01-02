package com.kyilmaz.neurocomet

/**
 * Splash screen configurations for different neurodivergent states.
 * Each configuration contains messages and visual style tailored to specific needs.
 */
data class SplashConfig(
    val messages: List<String>,
    val tagline: String,
    val animationStyle: SplashAnimationStyle,
    val durationMs: Long = 2000L
)

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
            messages = listOf("Welcome back", "Your space awaits", "Let's begin"),
            tagline = "A space designed for you",
            animationStyle = SplashAnimationStyle.CALM_WAVES
        )
        NeuroState.HYPERFOCUS -> SplashConfig(
            messages = listOf("Focus mode", "Clear mind ahead", "Ready to dive deep"),
            tagline = "Distraction-free zone",
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 1500L
        )
        NeuroState.OVERLOAD -> SplashConfig(
            messages = listOf("Taking it slow", "Breathe with me", "Gentle pace"),
            tagline = "Low stimulation mode",
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.CALM -> SplashConfig(
            messages = listOf("Peace awaits", "Serenity mode", "Calm waters"),
            tagline = "Your tranquil space",
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT
        )

        // ADHD states
        NeuroState.ADHD_ENERGIZED -> SplashConfig(
            messages = listOf("Let's go! ⚡", "Energy unlocked", "Time to shine"),
            tagline = "Ride the wave",
            animationStyle = SplashAnimationStyle.ENERGY_BURST,
            durationMs = 1500L
        )
        NeuroState.ADHD_LOW_DOPAMINE -> SplashConfig(
            messages = listOf("Warming up", "Finding your spark", "You've got this"),
            tagline = "Gentle motivation",
            animationStyle = SplashAnimationStyle.CREATIVE_SWIRL
        )
        NeuroState.ADHD_TASK_MODE -> SplashConfig(
            messages = listOf("Task mode", "One thing at a time", "Focus activated"),
            tagline = "Minimal distractions",
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 1200L
        )

        // Autism states
        NeuroState.AUTISM_ROUTINE -> SplashConfig(
            messages = listOf("Same as always", "Familiar patterns", "Comfort in routine"),
            tagline = "Predictable & safe",
            animationStyle = SplashAnimationStyle.ROUTINE_GRID
        )
        NeuroState.AUTISM_SENSORY_SEEK -> SplashConfig(
            messages = listOf("Sensory joy", "Feel the patterns", "Satisfying vibes"),
            tagline = "Rich experiences await",
            animationStyle = SplashAnimationStyle.SENSORY_SPARKLE
        )
        NeuroState.AUTISM_LOW_STIM -> SplashConfig(
            messages = listOf("Quiet mode", "Soft & gentle", "Rest your senses"),
            tagline = "Minimal stimulation",
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT,
            durationMs = 2500L
        )

        // Anxiety states
        NeuroState.ANXIETY_SOOTHE -> SplashConfig(
            messages = listOf("You're safe here", "Breathe in, breathe out", "All is well"),
            tagline = "Calming your mind",
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.ANXIETY_GROUNDING -> SplashConfig(
            messages = listOf("Feet on the ground", "Present moment", "Stable & secure"),
            tagline = "Rooted in now",
            animationStyle = SplashAnimationStyle.GROUNDING_EARTH
        )

        // Accessibility
        NeuroState.DYSLEXIA_FRIENDLY -> SplashConfig(
            messages = listOf("Clear & readable", "Your way", "Easy reading ahead"),
            tagline = "Designed for clarity",
            animationStyle = SplashAnimationStyle.FOCUS_PULSE
        )

        // Colorblind themes
        NeuroState.COLORBLIND_DEUTERANOPIA -> SplashConfig(
            messages = listOf("Colors optimized", "Blue & orange clarity", "See with confidence"),
            tagline = "Deuteranopia friendly",
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_PROTANOPIA -> SplashConfig(
            messages = listOf("Colors adapted", "Blue & yellow harmony", "Clear distinction"),
            tagline = "Protanopia friendly",
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_TRITANOPIA -> SplashConfig(
            messages = listOf("Colors refined", "Pink & teal contrast", "Visual clarity"),
            tagline = "Tritanopia friendly",
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.COLORBLIND_MONOCHROMACY -> SplashConfig(
            messages = listOf("Shapes & patterns", "High contrast", "Clear boundaries"),
            tagline = "Pattern-based design",
            animationStyle = SplashAnimationStyle.PATTERN_SHAPES
        )

        // Blind / Low Vision themes
        NeuroState.BLIND_SCREEN_READER -> SplashConfig(
            messages = listOf("Screen reader ready", "Accessibility first", "Welcome"),
            tagline = "Optimized for TalkBack",
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 2500L // Longer to allow screen reader to announce
        )
        NeuroState.BLIND_HIGH_CONTRAST -> SplashConfig(
            messages = listOf("Maximum contrast", "Clear and bold", "Easy to see"),
            tagline = "Pure black and white",
            animationStyle = SplashAnimationStyle.CONTRAST_RINGS
        )
        NeuroState.BLIND_LARGE_TEXT -> SplashConfig(
            messages = listOf("Large and clear", "Easy reading", "Your way"),
            tagline = "Extra large text mode",
            animationStyle = SplashAnimationStyle.FOCUS_PULSE,
            durationMs = 2500L
        )

        // Mood states
        NeuroState.MOOD_TIRED -> SplashConfig(
            messages = listOf("Take it easy", "No rush", "Gentle start"),
            tagline = "Rest is okay",
            animationStyle = SplashAnimationStyle.GENTLE_FLOAT,
            durationMs = 2500L
        )
        NeuroState.MOOD_ANXIOUS -> SplashConfig(
            messages = listOf("You're okay", "This too shall pass", "Safe space"),
            tagline = "Breathe with us",
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 2500L
        )
        NeuroState.MOOD_HAPPY -> SplashConfig(
            messages = listOf("Hello sunshine! ☀️", "Great vibes", "Joy awaits"),
            tagline = "Let's celebrate you",
            animationStyle = SplashAnimationStyle.ENERGY_BURST,
            durationMs = 1500L
        )
        NeuroState.MOOD_OVERWHELMED -> SplashConfig(
            messages = listOf("One breath", "Slowing down", "We've got you"),
            tagline = "Taking it gentle",
            animationStyle = SplashAnimationStyle.CALM_WAVES,
            durationMs = 3000L
        )
        NeuroState.MOOD_CREATIVE -> SplashConfig(
            messages = listOf("Inspiration incoming", "Create freely", "Imagination mode"),
            tagline = "Let ideas flow",
            animationStyle = SplashAnimationStyle.CREATIVE_SWIRL
        )

        // Secret Theme
        NeuroState.RAINBOW_BRAIN -> SplashConfig(
            messages = listOf(
                "🦄 You found the secret!",
                "🌈 Celebrate your unique mind",
                "✨ Neurodivergence is magic",
                "🧠 Your brain is beautiful",
                "💜 Different is powerful"
            ),
            tagline = "Embrace your rainbow brain",
            animationStyle = SplashAnimationStyle.RAINBOW_SPARKLE,
            durationMs = 2000L
        )
    }
}

