package com.kyilmaz.neurocomet

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException

/**
 * Thin, opinionated wrapper around the Jetpack [CredentialManager] API.
 *
 * ### What this gives NeuroComet
 *  - A passwordless **passkey** sign-in path, using the user's device
 *    biometric / screen lock to sign a server challenge. Google Play
 *    classifies passkeys as "strong" authentication, so turning them on
 *    raises our Play Protect score and reduces phishing-related support
 *    tickets materially.
 *  - **Saved passwords** from Google Password Manager (and compatible
 *    password managers) auto-suggest on the sign-in screen without us
 *    having to implement autofill hints manually.
 *
 * ### What is explicitly NOT implemented here
 *  - Server-side passkey registration (the server needs a WebAuthn
 *    `PublicKeyCredentialCreationOptions` issuer). This wrapper exposes
 *    create/get entry points as a string-in / string-out channel so the
 *    Supabase side can be wired up without rewriting this class.
 *  - OAuth "Sign in with Google" — intentionally deferred: the Data Safety
 *    form currently declares username+password+2FA; adding OAuth would
 *    require re-submitting the form.
 *
 * ### Error handling contract
 * All suspending methods return a sealed [PasskeyResult] instead of
 * throwing, so callers can render "No passkey set up yet" vs. "User
 * cancelled" differently without catching exceptions. This is critical
 * for neurodivergent-friendly UX where unexpected error toasts are
 * especially disorienting.
 */
object PasskeyManager {

    private const val TAG = "PasskeyManager"

    /**
     * Credential Manager is present from API 34 natively, and on API 26+
     * via the play-services-auth back-compat shim. We still gate to API 28
     * because passkey CREATION (not just retrieval) requires Android 9+.
     */
    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /**
     * Register a new passkey for the given WebAuthn creation challenge.
     *
     * @param requestJson Server-issued `PublicKeyCredentialCreationOptions`
     *                    serialized as a JSON string per the WebAuthn spec.
     * @return [PasskeyResult.Success] with the server-bound attestation response
     *         (also a JSON string), ready to POST back to the auth backend.
     */
    suspend fun registerPasskey(
        context: Context,
        requestJson: String,
    ): PasskeyResult {
        if (!isSupported()) return PasskeyResult.NotSupported
        val manager = CredentialManager.create(context)
        val request = CreatePublicKeyCredentialRequest(requestJson)
        return try {
            val response = manager.createCredential(context, request)
                    as CreatePublicKeyCredentialResponse
            PasskeyResult.Success(response.registrationResponseJson)
        } catch (e: CreateCredentialException) {
            Log.w(TAG, "registerPasskey failed: ${e.type}", e)
            PasskeyResult.Failure(e.message ?: e.type, cause = e)
        } catch (t: Throwable) {
            Log.e(TAG, "registerPasskey unexpected", t)
            PasskeyResult.Failure(t.message ?: "Unknown error", cause = t)
        }
    }

    /**
     * Sign in with a previously-registered passkey or a saved password.
     *
     * @param requestJson Server-issued `PublicKeyCredentialRequestOptions` JSON.
     *                    Pass `null` to request a saved password only.
     */
    suspend fun signIn(
        context: Context,
        requestJson: String? = null,
    ): PasskeyResult {
        if (!isSupported()) return PasskeyResult.NotSupported
        val manager = CredentialManager.create(context)
        val options = buildList {
            if (requestJson != null) {
                add(GetPublicKeyCredentialOption(requestJson))
            }
            add(GetPasswordOption())
        }
        val request = GetCredentialRequest(options)
        return try {
            val response: GetCredentialResponse = manager.getCredential(context, request)
            when (val cred = response.credential) {
                is PublicKeyCredential -> PasskeyResult.Success(cred.authenticationResponseJson)
                is PasswordCredential -> PasskeyResult.Password(cred.id, cred.password)
                else -> PasskeyResult.Failure("Unknown credential type: ${cred.type}")
            }
        } catch (e: NoCredentialException) {
            Log.i(TAG, "No credentials available")
            PasskeyResult.NoCredentials
        } catch (e: GetCredentialException) {
            Log.w(TAG, "signIn failed: ${e.type}", e)
            PasskeyResult.Failure(e.message ?: e.type, cause = e)
        } catch (t: Throwable) {
            Log.e(TAG, "signIn unexpected", t)
            PasskeyResult.Failure(t.message ?: "Unknown error", cause = t)
        }
    }
}

/** Outcome of a passkey / credential operation. */
sealed interface PasskeyResult {
    /** Jetpack Credential Manager isn't available on this Android version. */
    data object NotSupported : PasskeyResult
    /** No saved credentials found — caller should offer to register one. */
    data object NoCredentials : PasskeyResult
    /** Server-bound WebAuthn JSON ready to POST to the auth backend. */
    data class Success(val responseJson: String) : PasskeyResult
    /** A saved password was returned from the password manager. */
    data class Password(val username: String, val password: String) : PasskeyResult
    /** User cancelled, device error, etc. */
    data class Failure(val message: String, val cause: Throwable? = null) : PasskeyResult
}

