@file:Suppress("unused")

package com.kyilmaz.neurocomet

import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * Safe Supabase REST helpers that bypass kotlin-reflect's typeOf<T>() crash on Android.
 *
 * Error: "KotlinReflectionInternalError: Unresolved class: interface java.util.List"
 *
 * The Supabase SDK v3's reified functions (insert, select/decodeList, update) call
 * serializer<T>() which invokes typeOf<T>() at runtime. On Android/ART, kotlin-reflect
 * cannot resolve Java platform interfaces (java.util.List, java.util.Map) in the type
 * hierarchy, causing a crash.
 *
 * These helpers use a standalone Ktor HttpClient to call the PostgREST REST API directly,
 * encoding/decoding JSON manually and completely bypassing all reified typeOf() paths.
 */

/** Shared JSON parser (lenient to handle Supabase responses). */
private val supabaseJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** Standalone HTTP client without the Supabase SDK's URL rewriting. */
private val rawHttpClient by lazy { HttpClient(OkHttp) }

/** Cached raw Supabase URL (decrypted from BuildConfig). */
private val rawSupabaseUrl: String by lazy {
    SecurityUtils.decrypt(BuildConfig.SUPABASE_URL).removeSuffix("/")
}

/** Cached raw Supabase anon key (decrypted from BuildConfig). */
private val rawSupabaseKey: String by lazy {
    SecurityUtils.decrypt(BuildConfig.SUPABASE_KEY)
}

// =============================================================================
// INSERT
// =============================================================================

/**
 * Insert a single [JsonObject] row into a Supabase table.
 */
suspend fun SupabaseClient.safeInsert(table: String, value: JsonObject) {
    val response = rawHttpClient.post("$rawSupabaseUrl/rest/v1/$table") {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer $rawSupabaseKey")
        header("Prefer", "return=minimal")
        setBody(value.toString())
    }
    if (response.status.value !in 200..299) {
        throw Exception("Insert failed (${response.status}): ${response.bodyAsText()}")
    }
}

/**
 * Insert multiple [JsonObject] rows into a Supabase table.
 */
suspend fun SupabaseClient.safeInsertList(table: String, values: List<JsonObject>) {
    val body = JsonArray(values)
    val response = rawHttpClient.post("$rawSupabaseUrl/rest/v1/$table") {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer $rawSupabaseKey")
        header("Prefer", "return=minimal")
        setBody(body.toString())
    }
    if (response.status.value !in 200..299) {
        throw Exception("Bulk insert failed (${response.status}): ${response.bodyAsText()}")
    }
}

// =============================================================================
// SELECT
// =============================================================================

/**
 * Query rows from a Supabase table, returning raw [JsonArray].
 *
 * @param table   Table name
 * @param columns Comma-separated column list (e.g. "id,likes") or "*"
 * @param filters PostgREST filter query params (e.g. "post_id=eq.5&user_id=eq.abc")
 */
suspend fun safeSelect(
    table: String,
    columns: String = "*",
    filters: String = ""
): JsonArray {
    val url = buildString {
        append("$rawSupabaseUrl/rest/v1/$table?select=$columns")
        if (filters.isNotEmpty()) append("&$filters")
    }
    val response = rawHttpClient.get(url) {
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer $rawSupabaseKey")
        header("Accept", "application/json")
    }
    if (response.status.value !in 200..299) {
        throw Exception("Select failed (${response.status}): ${response.bodyAsText()}")
    }
    val text = response.bodyAsText()
    return supabaseJson.decodeFromString<JsonArray>(text)
}

// =============================================================================
// UPDATE (PATCH)
// =============================================================================

/**
 * Update rows in a Supabase table.
 *
 * @param table   Table name
 * @param body    JSON object with the fields to set
 * @param filters PostgREST filter query params (e.g. "id=eq.5")
 */
suspend fun safeUpdate(
    table: String,
    body: JsonObject,
    filters: String
) {
    val url = "$rawSupabaseUrl/rest/v1/$table?$filters"
    val response = rawHttpClient.patch(url) {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer $rawSupabaseKey")
        header("Prefer", "return=minimal")
        setBody(body.toString())
    }
    if (response.status.value !in 200..299) {
        throw Exception("Update failed (${response.status}): ${response.bodyAsText()}")
    }
}

// =============================================================================
// DELETE
// =============================================================================

/**
 * Delete rows from a Supabase table.
 *
 * @param table   Table name
 * @param filters PostgREST filter query params (e.g. "post_id=eq.5&user_id=eq.abc")
 */
suspend fun safeDelete(
    table: String,
    filters: String
) {
    val url = "$rawSupabaseUrl/rest/v1/$table?$filters"
    val response = rawHttpClient.delete(url) {
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer $rawSupabaseKey")
    }
    if (response.status.value !in 200..299) {
        throw Exception("Delete failed (${response.status}): ${response.bodyAsText()}")
    }
}
