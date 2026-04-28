package com.kyilmaz.neurocomet

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Single source of truth for the user's preferred in-app language (Per-App
 * Language Preferences — Android 13+, with back-compat for older APIs via
 * AppCompatDelegate).
 *
 * Storage mirrors [NeuroCometApplication.restoreSavedLocale] so a user-facing
 * picker and the automatic system restore read/write the same preference.
 */
object LanguagePreferences {

    private const val PREFS = "app_settings"
    private const val KEY_LOCALE = "selected_locale"

    /** ISO 639-1 tags we expose in the UI. `""` means "system default". */
    data class SupportedLanguage(
        /** BCP-47 tag, e.g. "en", "es", "fr", "" for system default. */
        val tag: String,
        /** Native-script display name, e.g. "Español". */
        val nativeName: String,
    )

    val SUPPORTED: List<SupportedLanguage> = listOf(
        SupportedLanguage("", "System default"),
        SupportedLanguage("en", "English"),
        SupportedLanguage("es", "Español"),
        SupportedLanguage("fr", "Français"),
        SupportedLanguage("de", "Deutsch"),
        SupportedLanguage("it", "Italiano"),
        SupportedLanguage("pt", "Português"),
        SupportedLanguage("nl", "Nederlands"),
        SupportedLanguage("pl", "Polski"),
        SupportedLanguage("tr", "Türkçe"),
        SupportedLanguage("sv", "Svenska"),
        SupportedLanguage("ar", "العربية"),
        SupportedLanguage("hi", "हिन्दी"),
        SupportedLanguage("zh", "中文"),
        SupportedLanguage("ja", "日本語"),
        SupportedLanguage("ko", "한국어"),
    )

    /** Returns the currently-applied language tag, or `""` for system default. */
    fun getCurrentTag(context: Context): String {
        val runtime = AppCompatDelegate.getApplicationLocales()
        if (!runtime.isEmpty) {
            return runtime.get(0)?.language.orEmpty()
        }
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LOCALE, "")
            .orEmpty()
    }

    /**
     * Applies the given language tag and persists it. Passing `""` reverts
     * to the system default locale.
     */
    fun apply(context: Context, tag: String) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (tag.isBlank()) {
            prefs.edit().remove(KEY_LOCALE).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            prefs.edit().putString(KEY_LOCALE, tag).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    /** Convenience: native-script name for the given tag (for the settings row). */
    fun displayNameFor(tag: String): String {
        if (tag.isBlank()) return "System default"
        return SUPPORTED.firstOrNull { it.tag == tag }?.nativeName
            ?: Locale.forLanguageTag(tag).getDisplayName(Locale.forLanguageTag(tag))
                .ifBlank { tag }
    }
}

