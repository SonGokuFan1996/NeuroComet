package com.kyilmaz.neuronetworkingtitle

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth // UPDATED: Was .gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    // Your Project URL and Anon Key
    private const val SUPABASE_URL = "https://xfylbfqposgllodadhka.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhmeWxiZnFwb3NnbGxvZGFkaGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ1NjQzNzIsImV4cCI6MjA4MDE0MDM3Mn0.AkaLs9Q5KqYPA3xRb_XjWHveoTZ2BtTgaeCfOO7z83c"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Configure JSON Serialization (Required for Post objects)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })

        install(Postgrest)
        install(Auth) // Uses the new Auth module
        install(Realtime)
    }
}