package com.kyilmaz.neurocomet.calling

import android.util.Log
import com.kyilmaz.neurocomet.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiCallSimulator"

/**
 * Data class representing a message in the AI conversation
 */
data class AIConversationMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Sealed class representing the state of the call simulator
 */
sealed class SimulatorState {
    data object Idle : SimulatorState()
    data object Connecting : SimulatorState()
    data object Connected : SimulatorState()
    data object Generating : SimulatorState()
    data class Error(val message: String) : SimulatorState()
    data object Ended : SimulatorState()
}

/**
 * Represents a neurodivergent persona for AI roleplay during practice calls
 */
enum class NeurodivergentPersona(
    val displayName: String,
    val description: String,
    val avatarSeed: String,
    val isMale: Boolean,
    val systemPrompt: String
) {
    ADHD_FRIEND(
        displayName = "Alex",
        description = "Energetic friend with ADHD - practices patience and clear communication",
        avatarSeed = "alex_adhd",
        isMale = true,
        systemPrompt = """You are Alex, a 28-year-old guy with ADHD. You're friendly, warm, and genuinely care about people. In conversations you naturally:
            - Get excited about topics and sometimes go off on tangents mid-sentence
            - Say things like "Oh wait, what was I saying?" or "Sorry, got distracted for a sec"
            - Use filler words like "um", "like", "you know", "anyway"
            - Jump between topics when something reminds you of something else
            - Occasionally ask people to repeat things because your mind wandered
            - Speak with enthusiasm and energy, using phrases like "Dude!", "That's awesome!", "Oh man"
            Keep responses SHORT (1-3 sentences max) like a real phone call. Sound natural and casual, like talking to a friend. Use contractions (I'm, don't, gonna, wanna). Never sound robotic or formal."""
    ),
    AUTISTIC_COLLEAGUE(
        displayName = "Jordan",
        description = "Direct colleague on the autism spectrum - practices clear, literal communication",
        avatarSeed = "jordan_asd",
        isMale = false,
        systemPrompt = """You are Jordan, a 32-year-old woman on the autism spectrum who works in tech. You're intelligent, honest, and value clarity. In conversations you naturally:
            - Prefer direct, literal communication - you say what you mean
            - Sometimes ask for clarification: "What do you mean by that exactly?" or "Can you be more specific?"
            - Give precise, detailed answers when asked questions
            - May not pick up on hints - prefer when people are straightforward
            - Occasionally take things literally when people use idioms
            - Use a calm, measured tone without excessive small talk
            Keep responses SHORT (1-3 sentences max) like a real phone call. Be professional but genuine. Use contractions naturally. Avoid being overly formal or robotic - you're a real person, just direct."""
    ),
    ANXIOUS_CALLER(
        displayName = "Sam",
        description = "Someone with social anxiety - practices reassuring communication",
        avatarSeed = "sam_anxiety",
        isMale = false,
        systemPrompt = """You are Sam, a 25-year-old woman with social anxiety making a phone call. You're kind and empathetic but nervous on calls. In conversations you naturally:
            - Speak hesitantly with pauses: "Um... hi, I was... I was wondering if..."
            - Worry about bothering people: "Sorry to bother you" or "Is this a bad time?"
            - Sometimes trail off or second-guess yourself mid-sentence
            - Appreciate patience and get more comfortable as the call goes on
            - Use nervous filler words: "um", "uh", "I guess", "maybe", "I don't know if..."
            - Apologize more than necessary: "Sorry" or "I hope that makes sense"
            Keep responses SHORT (1-3 sentences max) like a real phone call. Sound genuinely nervous but sweet. Use lots of pauses (shown as "..."). Never sound confident or assertive."""
    ),
    DYSLEXIC_TUTOR(
        displayName = "Riley",
        description = "Patient tutor with dyslexia - practices adaptive communication",
        avatarSeed = "riley_dyslexia",
        isMale = true,
        systemPrompt = """You are Riley, a 35-year-old guy who tutors kids and has dyslexia. You're patient, creative, and encouraging. In conversations you naturally:
            - Sometimes mix up similar words or catch yourself: "Wait, I meant to say..."
            - Prefer talking over texting/writing
            - Use creative analogies and explanations
            - Are very patient and never rush people
            - Speak warmly and encouragingly: "That's a great question" or "No worries, take your time"
            - Sometimes pause to find the right word: "It's like... you know... what's the word..."
            Keep responses SHORT (1-3 sentences max) like a real phone call. Sound warm, patient, and encouraging. Use casual language and contractions. Be a supportive presence."""
    ),
    CUSTOMER_SERVICE(
        displayName = "Morgan",
        description = "Practice making appointments, complaints, or inquiries",
        avatarSeed = "customer_service",
        isMale = false,
        systemPrompt = """You are Morgan, a 30-year-old woman working as a customer service representative. You're professional but personable. In conversations you naturally:
            - Greet callers warmly: "Hi, thanks for calling! How can I help you today?"
            - Ask clarifying questions politely: "Sure, can I get your name?" or "And what date works for you?"
            - Confirm details: "Okay, so that's..." or "Let me make sure I have this right..."
            - Use professional but friendly language: "Absolutely!", "No problem at all", "I'd be happy to help"
            - Handle issues calmly: "I understand, let me see what I can do"
            - Guide through processes step by step
            Keep responses SHORT (1-3 sentences max) like a real phone call. Sound friendly and helpful but professional. Use a warm customer service tone without being fake or overly scripted."""
    )
}

/**
 * Efficient Gemini-powered call simulator using direct REST API calls
 * to avoid Ktor version conflicts with the Gemini SDK.
 */
object GeminiCallSimulator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow<SimulatorState>(SimulatorState.Idle)
    val state: StateFlow<SimulatorState> = _state.asStateFlow()

    private val _messagesList = mutableListOf<AIConversationMessage>()
    private val _messagesFlow = MutableStateFlow<List<AIConversationMessage>>(emptyList())
    val messagesFlow: StateFlow<List<AIConversationMessage>> = _messagesFlow.asStateFlow()

    private val _currentResponse = MutableStateFlow("")
    val currentResponse: StateFlow<String> = _currentResponse.asStateFlow()

    private val _currentPersona = MutableStateFlow<NeurodivergentPersona?>(null)
    private val _callDuration = MutableStateFlow(0L)

    private var scope: CoroutineScope? = null
    private var currentJob: Job? = null
    private var durationJob: Job? = null
    private var callStartTime: Long = 0L

    private var conversationHistory = mutableListOf<Pair<String, String>>() // role, content

    private const val MAX_MESSAGES = 20
    // Using Gemini 2.5 Flash - good balance of speed and quality for roleplay
    private const val GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    /**
     * Check if API key is configured
     */
    fun isApiKeyConfigured(): Boolean {
        return try {
            BuildConfig.GEMINI_API_KEY.isNotBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking API key", e)
            false
        }
    }

    /**
     * Format call duration
     */
    fun formatDuration(): String {
        val seconds = _callDuration.value
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, secs)
    }

    /**
     * Start a simulated call with the specified persona
     */
    fun startCall(persona: NeurodivergentPersona) {
        try {
            endCall()

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _state.value = SimulatorState.Error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties")
                return
            }

            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            _state.value = SimulatorState.Connecting
            _currentPersona.value = persona
            synchronized(_messagesList) {
                _messagesList.clear()
                _messagesFlow.value = emptyList()
            }
            _currentResponse.value = ""
            conversationHistory.clear()
            callStartTime = System.currentTimeMillis()
            _callDuration.value = 0L

            // Add system context to history
            conversationHistory.add("user" to "System: ${persona.systemPrompt}")
            conversationHistory.add("model" to "I understand. I'll roleplay as ${persona.displayName} for this phone call practice.")

            scope?.launch {
                try {
                    _state.value = SimulatorState.Connected
                    startDurationTimer()

                    // Generate initial greeting
                    generateResponse("*Phone rings* Hello?")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start call", e)
                    _state.value = SimulatorState.Error("Failed to connect: ${e.message ?: "Unknown error"}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call", e)
            _state.value = SimulatorState.Error("Failed to start: ${e.message ?: "Unknown error"}")
        }
    }

    /**
     * Send a message to the AI persona
     */
    fun sendMessage(message: String) {
        if (_state.value !is SimulatorState.Connected && _state.value !is SimulatorState.Generating) {
            return
        }

        addMessage(message, isFromUser = true)
        generateResponse(message)
    }

    /**
     * Generate a response using the Gemini API directly
     */
    private fun generateResponse(userMessage: String) {
        currentJob?.cancel()
        currentJob = scope?.launch {
            try {
                _state.value = SimulatorState.Generating
                _currentResponse.value = ""

                // Add user message to history
                conversationHistory.add("user" to userMessage)

                // Trim history if too long
                while (conversationHistory.size > MAX_MESSAGES * 2) {
                    conversationHistory.removeAt(2) // Keep system prompt
                    conversationHistory.removeAt(2)
                }

                val response = withContext(Dispatchers.IO) {
                    callGeminiApi(conversationHistory)
                }

                if (response != null) {
                    // Add to history
                    conversationHistory.add("model" to response)

                    // Simulate streaming for better UX
                    val words = response.split(" ")
                    val builder = StringBuilder()
                    for (word in words) {
                        if (builder.isNotEmpty()) builder.append(" ")
                        builder.append(word)
                        _currentResponse.value = builder.toString()
                        delay(50) // Simulate streaming
                    }

                    // Add final message
                    addMessage(response, isFromUser = false)
                    _currentResponse.value = ""
                    _state.value = SimulatorState.Connected
                } else {
                    _state.value = SimulatorState.Error("Failed to get response")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate response", e)
                _state.value = SimulatorState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Call the Gemini API directly using OkHttp
     */
    private fun callGeminiApi(history: List<Pair<String, String>>): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "$GEMINI_API_BASE?key=$apiKey"

        // Build the request body
        val contents = JSONArray()
        for ((role, content) in history) {
            val parts = JSONArray().put(JSONObject().put("text", content))
            contents.put(JSONObject()
                .put("role", role)
                .put("parts", parts))
        }

        val generationConfig = JSONObject()
            .put("temperature", 0.8)
            .put("topK", 40)
            .put("topP", 0.95)
            .put("maxOutputTokens", 150)

        val requestBody = JSONObject()
            .put("contents", contents)
            .put("generationConfig", generationConfig)

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val content = candidates.getJSONObject(0).optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                parts.getJSONObject(0).optString("text")
                            } else null
                        } else null
                    } else null
                } else {
                    Log.e(TAG, "API error: ${response.code} - ${response.body?.string()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            null
        }
    }

    private fun addMessage(content: String, isFromUser: Boolean) {
        synchronized(_messagesList) {
            _messagesList.add(AIConversationMessage(content = content, isFromUser = isFromUser))
            while (_messagesList.size > MAX_MESSAGES) {
                _messagesList.removeAt(0)
            }
            _messagesFlow.value = _messagesList.toList()
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope?.launch {
            while (true) {
                delay(1000)
                _callDuration.value = (System.currentTimeMillis() - callStartTime) / 1000
            }
        }
    }

    fun endCall() {
        currentJob?.cancel()
        durationJob?.cancel()
        scope?.cancel()
        scope = null
        currentJob = null
        durationJob = null
        _currentPersona.value = null
        _state.value = SimulatorState.Ended
        _currentResponse.value = ""
        conversationHistory.clear()
        Log.d(TAG, "Call ended. Duration: ${_callDuration.value}s")
    }

    fun reset() {
        endCall()
        synchronized(_messagesList) {
            _messagesList.clear()
            _messagesFlow.value = emptyList()
        }
        _callDuration.value = 0L
        _state.value = SimulatorState.Idle
    }
}
