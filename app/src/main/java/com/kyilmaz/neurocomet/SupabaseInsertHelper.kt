@file:Suppress("unused")

package com.kyilmaz.neurocomet

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
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
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

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

private const val TAG = "SupabaseREST"

private data class RestAuthContext(
    val bearerToken: String,
    val usingUserJwt: Boolean,
    val userId: String?
)

/** Shared JSON parser (lenient to handle Supabase responses). */
private val supabaseJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** Standalone HTTP client without the Supabase SDK's URL rewriting. */
private val rawHttpClient by lazy { HttpClient(OkHttp) }

/** Cached raw Supabase URL (decrypted from BuildConfig). */
private val rawSupabaseUrl: String by lazy {
    SecurityUtils.decrypt(BuildConfig.SUPABASE_URL)
        .removeSuffix("/")
        .removeSuffix("/rest/v1")   // guard against double-path
        .removeSuffix("/")
}

/** Cached raw Supabase anon key (decrypted from BuildConfig). */
private val rawSupabaseKey: String by lazy {
    SecurityUtils.decrypt(BuildConfig.SUPABASE_KEY)
}

/**
 * Returns the current authenticated user's JWT access token if available,
 * otherwise falls back to the anon key.
 *
 * RLS policies that check `auth.uid()` require a real user JWT — sending
 * only the anon key causes those policies to evaluate uid as NULL and
 * reject (or silently skip) the operation, leading to 500 / empty-result
 * errors.
 */
private suspend fun resolveAuthContext(client: SupabaseClient? = null): RestAuthContext {
    return try {
        val authClient = client ?: AppSupabaseClient.client
        var session = authClient?.auth?.currentSessionOrNull()
        var accessToken = session?.accessToken?.takeIf { it.isNotBlank() }
        val currentUserId = authClient?.auth?.currentUserOrNull()?.id

        if (accessToken == null && currentUserId != null) {
            repeat(5) { attempt ->
                if (accessToken != null) return@repeat
                delay(150)
                session = authClient.auth.currentSessionOrNull()
                accessToken = session?.accessToken?.takeIf { it.isNotBlank() }
                if (accessToken != null) {
                    Log.d(TAG, "Recovered user JWT for ${currentUserId.take(8)}… after auth propagation delay (${'$'}{attempt + 1}/5)")
                    return@repeat
                }
                if (attempt == 4) {
                    Log.w(TAG, "Authenticated user ${currentUserId.take(8)}… still has no session JWT; falling back to anon key")
                }
            }
        }

        if (accessToken != null) {
            RestAuthContext(
                bearerToken = accessToken,
                usingUserJwt = true,
                userId = session?.user?.id
            )
        } else {
            RestAuthContext(
                bearerToken = rawSupabaseKey,
                usingUserJwt = false,
                userId = authClient?.auth?.currentUserOrNull()?.id
            )
        }
    } catch (e: Exception) {
        Log.w(TAG, "Falling back to anon key for REST request", e)
        RestAuthContext(
            bearerToken = rawSupabaseKey,
            usingUserJwt = false,
            userId = null
        )
    }
}

private fun buildRestFailureMessage(
    operation: String,
    table: String,
    status: Int,
    body: String,
    authContext: RestAuthContext
): String {
    val normalizedBody = body.replace('\n', ' ').trim()
    val authMode = if (authContext.usingUserJwt) "user-jwt" else "anon"
    val hint = when {
        status == 401 -> {
            if (authContext.usingUserJwt) {
                "The authenticated session may be expired; sign in again to refresh the JWT."
            } else {
                "No active user JWT was available, so the request was sent with the anon key."
            }
        }
        normalizedBody.contains("row-level security", ignoreCase = true) || normalizedBody.contains("\"42501\"") -> {
            if (authContext.usingUserJwt) {
                "The table's RLS policy rejected this authenticated user for `$table`."
            } else {
                "RLS evaluated the request as anonymous because no user JWT was attached."
            }
        }
        normalizedBody.contains("violates foreign key constraint", ignoreCase = true) &&
            normalizedBody.contains("user", ignoreCase = true) -> {
            "A related `public.users` row is missing for this account; backfill the user row and retry."
        }
        normalizedBody.contains("relation", ignoreCase = true) &&
            normalizedBody.contains("does not exist", ignoreCase = true) -> {
            "The required Supabase table/schema is missing; run the setup SQL migration."
        }
        normalizedBody.contains("schema cache", ignoreCase = true) &&
            normalizedBody.contains("could not find the", ignoreCase = true) -> {
            "The deployed Supabase table is older than this app expects; run `supabase/setup_required_now.sql` (or add the missing columns) and retry."
        }
        else -> null
    }

    return buildString {
        append("$operation failed ($status, auth=$authMode")
        authContext.userId?.takeIf { it.isNotBlank() }?.let { append(", user=${it.take(8)}…") }
        append(") on `$table`: $normalizedBody")
        hint?.let { append(" Hint: $it") }
    }
}

// =============================================================================
// INSERT
// =============================================================================

/**
 * Insert a single [JsonObject] row into a Supabase table.
 * @return the inserted row if [returnRepresentation] is true, null otherwise.
 */
suspend fun SupabaseClient.safeInsert(
    table: String,
    value: JsonObject,
    returnRepresentation: Boolean = false
): JsonObject? {
    val url = "$rawSupabaseUrl/rest/v1/$table"
    val authContext = resolveAuthContext(this)
    Log.d(TAG, "INSERT → $table (auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
        if (returnRepresentation) {
            header("Prefer", "return=representation")
        } else {
            header("Prefer", "return=minimal")
        }
        setBody(value.toString())
    }
    if (response.status.value !in 200..299) {
        val body = response.bodyAsText()
        val message = buildRestFailureMessage("Insert", table, response.status.value, body, authContext)
        Log.e(TAG, message)
        throw Exception(message)
    }
    return if (returnRepresentation) {
        val text = response.bodyAsText()
        // representation returns an array of 1 element if single insert
        supabaseJson.decodeFromString<JsonArray>(text).firstOrNull()?.jsonObject
    } else null
}

/**
 * Insert multiple [JsonObject] rows into a Supabase table.
 */
suspend fun SupabaseClient.safeInsertList(table: String, values: List<JsonObject>) {
    val body = JsonArray(values)
    val url = "$rawSupabaseUrl/rest/v1/$table"
    val authContext = resolveAuthContext(this)
    Log.d(TAG, "BULK INSERT → $table (${values.size} rows, auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
        header("Prefer", "return=minimal")
        setBody(body.toString())
    }
    if (response.status.value !in 200..299) {
        val respBody = response.bodyAsText()
        val message = buildRestFailureMessage("Bulk insert", table, response.status.value, respBody, authContext)
        Log.e(TAG, message)
        throw Exception(message)
    }
}

/**
 * Upsert a single [JsonObject] row into a Supabase table.
 * Uses PostgREST `resolution=merge-duplicates` to perform INSERT … ON CONFLICT DO UPDATE.
 * Requires the table to have a primary key or unique constraint on the conflicting columns.
 */
suspend fun SupabaseClient.safeUpsert(
    table: String,
    value: JsonObject,
    onConflict: String = "id"
): JsonObject? {
    val url = "$rawSupabaseUrl/rest/v1/$table?on_conflict=$onConflict"
    val authContext = resolveAuthContext(this)
    Log.d(TAG, "UPSERT → $table (on_conflict=$onConflict, auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
        header("Prefer", "return=minimal,resolution=merge-duplicates")
        setBody(value.toString())
    }
    if (response.status.value !in 200..299) {
        val body = response.bodyAsText()
        val message = buildRestFailureMessage("Upsert", table, response.status.value, body, authContext)
        Log.e(TAG, message)
        throw Exception(message)
    }
    return null
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
    val authContext = resolveAuthContext()
    Log.d(TAG, "SELECT → $table (columns=$columns, auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.get(url) {
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
        header("Accept", "application/json")
    }
    if (response.status.value !in 200..299) {
        val body = response.bodyAsText()
        val message = buildRestFailureMessage("Select", table, response.status.value, body, authContext)
        Log.e(TAG, message)
        throw Exception(message)
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
    val authContext = resolveAuthContext()
    Log.d(TAG, "UPDATE → $table ($filters, auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.patch(url) {
        contentType(ContentType.Application.Json)
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
        header("Prefer", "return=minimal")
        setBody(body.toString())
    }
    if (response.status.value !in 200..299) {
        val respBody = response.bodyAsText()
        val message = buildRestFailureMessage("Update", table, response.status.value, respBody, authContext)
        Log.e(TAG, message)
        throw Exception(message)
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
    val authContext = resolveAuthContext()
    Log.d(TAG, "DELETE → $table ($filters, auth=${if (authContext.usingUserJwt) "user-jwt" else "anon"})")
    val response = rawHttpClient.delete(url) {
        header("apikey", rawSupabaseKey)
        header("Authorization", "Bearer ${authContext.bearerToken}")
    }
    if (response.status.value !in 200..299) {
        val body = response.bodyAsText()
        val message = buildRestFailureMessage("Delete", table, response.status.value, body, authContext)
        Log.e(TAG, message)
        throw Exception(message)
    }
}
