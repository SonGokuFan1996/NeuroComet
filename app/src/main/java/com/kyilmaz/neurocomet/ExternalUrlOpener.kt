package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale

private fun String?.normalizedHost(): String? = this?.trim()?.lowercase(Locale.US)?.removeSuffix(".")

private fun isPrivateOrUnsafeHost(host: String): Boolean {
    val normalized = host.normalizedHost() ?: return true
    if (normalized.isBlank()) return true
    if (normalized == "localhost" || normalized.endsWith(".localhost")) return true
    if (normalized == "127.0.0.1" || normalized == "0.0.0.0" || normalized == "::1") return true
    if (normalized.startsWith("10.") || normalized.startsWith("192.168.")) return true
    if (normalized.matches(Regex("^172\\.(1[6-9]|2\\d|3[0-1])\\..*"))) return true
    if (normalized.startsWith("169.254.")) return true
    if (normalized.startsWith("xn--")) return true
    return false
}

fun isSafePublicHttpsUrl(url: String, allowedHosts: Set<String>? = null): Boolean {
    val uri = runCatching { Uri.parse(url.trim()) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase(Locale.US)
    val host = uri.host.normalizedHost() ?: return false
    if (scheme != "https") return false
    if (uri.userInfo != null) return false
    if (isPrivateOrUnsafeHost(host)) return false

    return allowedHosts?.takeIf { it.isNotEmpty() }?.any { allowedHost ->
        val normalizedAllowed = allowedHost.normalizedHost() ?: return@any false
        host == normalizedAllowed || host.endsWith(".$normalizedAllowed")
    } ?: true
}

fun openSafeExternalUrl(
    context: Context,
    url: String,
    allowedHosts: Set<String>? = null
): Boolean {
    if (!isSafePublicHttpsUrl(url, allowedHosts)) {
        return false
    }

    val uri = Uri.parse(url.trim())
    val intent = Intent(Intent.ACTION_VIEW, uri)
        .addCategory(Intent.CATEGORY_BROWSABLE)

    if (intent.resolveActivity(context.packageManager) == null) {
        return false
    }

    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

fun openTrustedExternalUrl(
    context: Context,
    url: String,
    allowedHosts: Set<String>
): Boolean {
    return openSafeExternalUrl(context, url, allowedHosts)
}

