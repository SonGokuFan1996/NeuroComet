package com.kyilmaz.neurocomet.calling

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "CallAudioManager"

/**
 * Manages Text-to-Speech for AI call responses
 * Makes the AI persona "speak" their responses aloud
 */
class CallAudioManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentIsMale = true

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS Language not supported")
                } else {
                    isInitialized = true
                    _isReady.value = true
                    Log.d(TAG, "TTS initialized successfully")

                    // Set up listener for speaking status
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            Log.e(TAG, "TTS error for utterance: $utteranceId")
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                            Log.e(TAG, "TTS error $errorCode for utterance: $utteranceId")
                        }
                    })
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
        }
    }

    /**
     * Configure voice settings for male or female persona
     * Adjusts pitch and speech rate to sound more natural
     */
    fun setVoiceForGender(isMale: Boolean) {
        if (!isInitialized) return
        currentIsMale = isMale

        if (isMale) {
            // Male voice: lower pitch, slightly slower
            tts?.setPitch(0.85f)
            tts?.setSpeechRate(0.95f)
        } else {
            // Female voice: higher pitch, slightly faster
            tts?.setPitch(1.15f)
            tts?.setSpeechRate(1.05f)
        }
        Log.d(TAG, "Voice configured for ${if (isMale) "male" else "female"}")
    }

    /**
     * Speak the given text aloud
     * @param text The text to speak
     * @param utteranceId Optional ID for tracking this utterance
     */
    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, cannot speak")
            return
        }

        // Clean up the text for better speech
        val cleanText = text
            .replace("*", "") // Remove asterisks used for actions
            .replace("...", ", ") // Replace ellipsis with pause
            .trim()

        if (cleanText.isBlank()) return

        tts?.speak(cleanText, TextToSpeech.QUEUE_ADD, null, utteranceId)
        Log.d(TAG, "Speaking: ${cleanText.take(50)}...")
    }

    /**
     * Stop any current speech
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Set the speech pitch (0.5 = low, 1.0 = normal, 2.0 = high)
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set the speech rate (0.5 = slow, 1.0 = normal, 2.0 = fast)
     */
    fun setRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Release TTS resources
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isReady.value = false
        _isSpeaking.value = false
        Log.d(TAG, "TTS shutdown")
    }
}

