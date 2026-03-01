package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openTrustedExternalUrl(
    context: Context,
    url: String,
    allowedHosts: Set<String>
): Boolean {
    val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase()
    val host = uri.host?.lowercase()
    val isAllowed = scheme == "https" && host != null && allowedHosts.any { allowedHost ->
        host == allowedHost || host.endsWith(".$allowedHost")
    }
    if (!isAllowed) {
        return false
    }

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

