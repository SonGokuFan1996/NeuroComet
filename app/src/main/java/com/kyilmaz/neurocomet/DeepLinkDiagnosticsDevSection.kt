package com.kyilmaz.neurocomet

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Developer-only diagnostics for Android App Links + share-URL generation.
 *
 * Validates the full round-trip for `https://getneurocomet.com/post/{id}`:
 *   1. Every share URL the app generates uses `AppLinks.CANONICAL_DOMAIN`.
 *   2. For each verified host+path, AndroidManifest declares a matching
 *      `<intent-filter>` (probed via PackageManager).
 *   3. `DomainVerificationManager` reports our hosts as verified
 *      (Android 12+ only; older versions auto-verify silently).
 *   4. `https://getneurocomet.com/.well-known/assetlinks.json` is reachable,
 *      served as `application/json`, and references our package name.
 *
 * If any check fails, the row shows why, so we catch a broken deployment
 * (DNS misconfigured, wrong fingerprint in assetlinks, etc.) before users
 * experience it as "tapping a shared post opens Chrome instead of the app."
 */
@Composable
fun DeepLinkDiagnosticsDevSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var samplePostId by remember { mutableStateOf("12345") }
    var sampleUserId by remember { mutableStateOf("alex") }
    var verifyState by remember { mutableStateOf<DeepLinkVerificationReport?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var assetlinksReport by remember { mutableStateOf<AssetLinksReport?>(null) }
    var assetlinksRunning by remember { mutableStateOf(false) }

    DevSectionCard(title = "Deep Link / App Links", icon = Icons.Filled.Link) {

        // ─── Canonical config block ─────────────────────────────
        Text("Canonical config", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        DiagKV("Domain", AppLinks.CANONICAL_DOMAIN)
        DiagKV("Origin", AppLinks.CANONICAL_ORIGIN)
        DiagKV("Hosts", AppLinks.VERIFIED_HOSTS.joinToString())
        DiagKV("Paths", AppLinks.VERIFIED_PATH_PREFIXES.joinToString())
        DiagKV("Support", AppLinks.SUPPORT_EMAIL)

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ─── URL preview / round-trip ───────────────────────────
        Text("URL preview & round-trip", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = samplePostId,
            onValueChange = { samplePostId = it.filter { c -> c.isLetterOrDigit() || c == '-' } },
            label = { Text("Sample post ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        val postUrl = remember(samplePostId) { AppLinks.postUrl(samplePostId.ifBlank { "0" }) }
        UrlRow(url = postUrl)

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = sampleUserId,
            onValueChange = { sampleUserId = it.trim() },
            label = { Text("Sample user handle") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        val userUrl = remember(sampleUserId) { AppLinks.profileUrl(sampleUserId.ifBlank { "user" }) }
        UrlRow(url = userUrl)

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ─── Manifest + App Links verification ──────────────────
        Text("Manifest & verification status", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !isRunning,
                onClick = {
                    isRunning = true
                    scope.launch {
                        val report = withContext(Dispatchers.IO) {
                            runManifestAndVerificationChecks(context)
                        }
                        verifyState = report
                        isRunning = false
                    }
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isRunning) "Running…" else "Run checks")
            }
        }

        verifyState?.let { rep ->
            Spacer(Modifier.height(10.dp))
            rep.rows.forEach { row ->
                StatusRow(row)
            }
            rep.summary?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ─── assetlinks.json reachability ───────────────────────
        Text("assetlinks.json probe", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            AppLinks.ASSETLINKS_URL,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !assetlinksRunning,
                onClick = {
                    assetlinksRunning = true
                    scope.launch {
                        val report = withContext(Dispatchers.IO) { probeAssetLinks(context.packageName) }
                        assetlinksReport = report
                        assetlinksRunning = false
                    }
                }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (assetlinksRunning) "Fetching…" else "Probe assetlinks.json")
            }
            OutlinedButton(onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.ASSETLINKS_URL))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Open in browser")
            }
        }

        assetlinksReport?.let { rep ->
            Spacer(Modifier.height(10.dp))
            rep.rows.forEach { StatusRow(it) }
            rep.rawBody?.let {
                Spacer(Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        it.take(1200),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Rendering helpers
// ──────────────────────────────────────────────────────────────

@Composable
private fun DiagKV(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(k, modifier = Modifier.width(86.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(v, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun UrlRow(url: String) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(url, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = {
                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("url", url))
                        android.widget.Toast.makeText(context, "Copied", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("Copy") },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) }
                )
                AssistChip(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "No handler: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    label = { Text("Fire VIEW intent") },
                    leadingIcon = { Icon(Icons.Filled.OpenInBrowser, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun StatusRow(row: StatusRowData) {
    val (icon, tint) = when (row.status) {
        Status.OK -> Icons.Filled.CheckCircle to Color(0xFF2E7D32)
        Status.WARN -> Icons.Filled.Warning to Color(0xFFEF6C00)
        Status.FAIL -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
        Status.INFO -> Icons.Filled.Link to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(row.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (row.detail.isNotBlank()) {
                Text(row.detail, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Diagnostic data + workers
// ──────────────────────────────────────────────────────────────

private enum class Status { OK, WARN, FAIL, INFO }
private data class StatusRowData(val status: Status, val title: String, val detail: String = "")
private data class DeepLinkVerificationReport(val rows: List<StatusRowData>, val summary: String? = null)
private data class AssetLinksReport(val rows: List<StatusRowData>, val rawBody: String? = null)

private fun runManifestAndVerificationChecks(context: android.content.Context): DeepLinkVerificationReport {
    val rows = mutableListOf<StatusRowData>()
    val pm = context.packageManager

    // 1. Manifest intent-filter probe: for each host+path we expect, ask
    //    PackageManager to resolve a VIEW intent and confirm our own package
    //    is among the handlers.
    for (host in AppLinks.VERIFIED_HOSTS) {
        for (path in AppLinks.VERIFIED_PATH_PREFIXES) {
            val probeUrl = "https://$host${path}diagnostic-probe-0"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(probeUrl))
            val resolvers: List<ResolveInfo> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                }
            val ourPkg = context.packageName
            val ourMatch = resolvers.any { it.activityInfo.packageName == ourPkg }
            rows += if (ourMatch) {
                StatusRowData(Status.OK, "Manifest filter: $host$path", "$ourPkg is a handler")
            } else {
                StatusRowData(
                    Status.FAIL,
                    "Manifest filter: $host$path",
                    "Our package NOT in handler list — intent-filter missing"
                )
            }
        }
    }

    // 2. Domain verification status (Android 12+). Older API levels use the
    //    legacy autoVerify flow which has no public readback, so we just report INFO.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val dvm = context.getSystemService(android.content.pm.verify.domain.DomainVerificationManager::class.java)
            val state = dvm.getDomainVerificationUserState(context.packageName)
            val map = state?.hostToStateMap ?: emptyMap()
            for (host in AppLinks.VERIFIED_HOSTS) {
                val code = map[host]
                val row = when (code) {
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_VERIFIED ->
                        StatusRowData(Status.OK, "App Links verified: $host", "DomainVerificationManager = VERIFIED")
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_SELECTED ->
                        StatusRowData(Status.WARN, "User-selected (not auto-verified): $host",
                            "User opted in manually; assetlinks.json likely failing")
                    android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_NONE ->
                        StatusRowData(Status.WARN, "Not verified: $host",
                            "Run `adb shell pm verify-app-links --re-verify ${context.packageName}` after assetlinks.json is live")
                    null ->
                        StatusRowData(Status.FAIL, "Not declared: $host", "Not present in hostToStateMap — check manifest")
                    else ->
                        StatusRowData(Status.INFO, "Unknown state ($code): $host")
                }
                rows += row
            }
        } catch (e: Throwable) {
            rows += StatusRowData(Status.WARN, "DomainVerificationManager unavailable", e.message ?: "")
        }
    } else {
        rows += StatusRowData(
            Status.INFO,
            "Android < 12 device",
            "Legacy autoVerify; no public readback — check `adb shell pm get-app-links`"
        )
    }

    // 3. Scan share-URL generator: sanity check that AppLinks produces the
    //    expected shape.
    val samplePost = AppLinks.postUrl(42)
    val expectedPrefix = "${AppLinks.CANONICAL_ORIGIN}/post/"
    rows += if (samplePost.startsWith(expectedPrefix)) {
        StatusRowData(Status.OK, "AppLinks.postUrl(42) shape", samplePost)
    } else {
        StatusRowData(Status.FAIL, "AppLinks.postUrl() wrong shape", samplePost)
    }

    val summary = buildString {
        val fails = rows.count { it.status == Status.FAIL }
        val warns = rows.count { it.status == Status.WARN }
        val oks = rows.count { it.status == Status.OK }
        append("$oks passed, $warns warnings, $fails failed.")
        if (fails > 0) append("  Fix manifest/DNS/assetlinks before release.")
    }
    return DeepLinkVerificationReport(rows, summary)
}

private fun probeAssetLinks(expectedPackage: String): AssetLinksReport {
    val rows = mutableListOf<StatusRowData>()
    var body: String? = null
    val conn = try {
        (URL(AppLinks.ASSETLINKS_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
            instanceFollowRedirects = false // App Links forbids redirects
            requestMethod = "GET"
        }
    } catch (e: Throwable) {
        rows += StatusRowData(Status.FAIL, "Cannot open connection", e.message ?: e.javaClass.simpleName)
        return AssetLinksReport(rows)
    }

    try {
        val code = conn.responseCode
        val ctype = conn.contentType ?: ""
        rows += when {
            code == 200 -> StatusRowData(Status.OK, "HTTP $code OK")
            code in 300..399 -> StatusRowData(Status.FAIL, "HTTP $code redirect", "Google requires a 200 with NO redirects")
            else -> StatusRowData(Status.FAIL, "HTTP $code", conn.responseMessage ?: "")
        }
        rows += if (ctype.contains("application/json", ignoreCase = true)) {
            StatusRowData(Status.OK, "Content-Type", ctype)
        } else {
            StatusRowData(Status.FAIL, "Content-Type not JSON", "Got: '$ctype' — must be application/json")
        }
        if (code == 200) {
            body = conn.inputStream.bufferedReader().use { it.readText() }
            try {
                val arr = JSONArray(body)
                var sawPackage = false
                var sawPlaceholder = false
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val target = obj.optJSONObject("target") ?: continue
                    val pkg = target.optString("package_name")
                    if (pkg == expectedPackage) sawPackage = true
                    val fps = target.optJSONArray("sha256_cert_fingerprints")
                    if (fps != null) {
                        for (j in 0 until fps.length()) {
                            val fp = fps.getString(j)
                            if (fp.startsWith("REPLACE_") || fp.isBlank()) sawPlaceholder = true
                        }
                    }
                }
                rows += if (sawPackage) {
                    StatusRowData(Status.OK, "package_name match", expectedPackage)
                } else {
                    StatusRowData(Status.FAIL, "package_name missing", "$expectedPackage not found in JSON")
                }
                rows += if (sawPlaceholder) {
                    StatusRowData(Status.FAIL, "Fingerprint placeholder present", "Replace REPLACE_WITH_* with real SHA-256")
                } else {
                    StatusRowData(Status.OK, "No fingerprint placeholders")
                }
            } catch (e: Throwable) {
                rows += StatusRowData(Status.FAIL, "JSON parse error", e.message ?: "")
            }
        }
    } catch (e: Throwable) {
        rows += StatusRowData(Status.FAIL, "Request failed", e.message ?: e.javaClass.simpleName)
    } finally {
        try { conn.disconnect() } catch (_: Throwable) {}
    }
    return AssetLinksReport(rows, body)
}

