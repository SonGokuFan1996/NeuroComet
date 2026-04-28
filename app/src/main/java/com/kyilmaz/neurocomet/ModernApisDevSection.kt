package com.kyilmaz.neurocomet

import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.launch

/**
 * Dev tool that exercises each of the five modern Android APIs we recently
 * adopted. Visible only inside Developer Options so QA / the household can
 * smoke-test every surface without touching production flows.
 *
 *   1. Android Photo Picker (PickVisualMedia) — permission-free image pick
 *   2. Per-App Language Preferences — runtime locale swap
 *   3. App Shortcuts — push a dynamic shortcut & report usage
 *   4. Predictive Back — live progress scrubber on a dummy card
 *   5. Credential Manager / Passkeys — sign-in + register probes
 */
@Composable
fun ModernApisDevSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ─── 1. Photo Picker ─────────────────────────────────────
    var pickedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> pickedUri = uri }

    // ─── 4. Predictive Back demo state ──────────────────────
    var demoCardVisible by remember { mutableStateOf(false) }
    var backProgress by remember { mutableFloatStateOf(0f) }
    if (demoCardVisible) {
        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { event -> backProgress = event.progress }
                backProgress = 0f
                demoCardVisible = false
            } catch (_: kotlinx.coroutines.CancellationException) {
                backProgress = 0f
            }
        }
    }

    // ─── 5. Passkey result display ──────────────────────────
    var passkeyStatus by remember { mutableStateOf("Not attempted yet") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Smoke tests for the five modern Android APIs that shipped " +
                    "alongside the audit.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // 1. Photo Picker ────────────────────────────────────
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Photo Picker", fontWeight = FontWeight.Bold)
                Text(
                    "Launches PickVisualMedia — no READ_MEDIA_IMAGES prompt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }) { Text("Pick an image") }
                if (pickedUri != null) {
                    Text(
                        "✓ Picked: ${pickedUri}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // 2. Per-App Language ────────────────────────────────
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2. Per-App Language", fontWeight = FontWeight.Bold)
                Text(
                    "Currently applied: ${
                        LanguagePreferences.displayNameFor(
                            LanguagePreferences.getCurrentTag(context)
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf("", "en", "es", "fr", "de", "tr", "ja").forEach { tag ->
                        AssistChip(
                            onClick = {
                                LanguagePreferences.apply(context, tag)
                                Toast.makeText(
                                    context,
                                    "Locale → ${if (tag.isBlank()) "system" else tag}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            label = {
                                Text(
                                    if (tag.isBlank()) "system"
                                    else LanguagePreferences.displayNameFor(tag),
                                )
                            }
                        )
                    }
                }
            }
        }

        // 3. App Shortcuts ───────────────────────────────────
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("3. App Shortcuts", fontWeight = FontWeight.Bold)
                Text(
                    "Long-press the app icon to see the 3 static shortcuts. " +
                            "Use the button below to push a dynamic one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = {
                    runCatching {
                        val intent = android.content.Intent(
                            AppShortcutsManager.ACTION_START_REGULATION
                        ).apply {
                            setPackage(context.packageName)
                            setClassName(
                                context.packageName,
                                "com.kyilmaz.neurocomet.MainActivity",
                            )
                            action = AppShortcutsManager.ACTION_START_REGULATION
                        }
                        val shortcut = ShortcutInfoCompat.Builder(context, "dev_regulation")
                            .setShortLabel("Regulate (dev)")
                            .setLongLabel("Dev — start regulation session")
                            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_regulation))
                            .setIntent(intent)
                            .build()
                        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
                        Toast.makeText(context, "Dynamic shortcut pushed", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Push dynamic shortcut") }
            }
        }

        // 4. Predictive Back ─────────────────────────────────
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("4. Predictive Back", fontWeight = FontWeight.Bold)
                Text(
                    "Tap to open the demo card, then swipe from the edge to " +
                            "watch the progress-driven scrub. Release to commit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { demoCardVisible = true }) {
                    Text("Open demo card")
                }
                if (demoCardVisible) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .graphicsLayer {
                                val s = 1f - backProgress * 0.08f
                                scaleX = s; scaleY = s
                                alpha = 1f - backProgress * 0.35f
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                "Swipe back from screen edge — progress: ${"%.2f".format(backProgress)}",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }

        // 5. Credential Manager ──────────────────────────────
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("5. Credential Manager", fontWeight = FontWeight.Bold)
                Text(
                    "Probes the Jetpack CredentialManager API. Register needs a " +
                            "WebAuthn challenge from our auth backend, so this button " +
                            "feeds a deliberately-bogus JSON and surfaces the error path.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Supported on this device: ${PasskeyManager.isSupported()}",
                    style = MaterialTheme.typography.bodySmall,
                )
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            passkeyStatus = "Signing in…"
                            passkeyStatus = when (val r = PasskeyManager.signIn(context)) {
                                is PasskeyResult.Success -> "✓ Passkey OK (${r.responseJson.length} chars)"
                                is PasskeyResult.Password -> "✓ Password for ${r.username}"
                                PasskeyResult.NoCredentials -> "No saved credentials"
                                PasskeyResult.NotSupported -> "Device unsupported"
                                is PasskeyResult.Failure -> "✗ ${r.message}"
                            }
                        }
                    }) { Text("Sign-in probe") }

                    OutlinedButton(onClick = {
                        scope.launch {
                            passkeyStatus = "Registering…"
                            val bogusChallenge = """{"challenge":"AAAA","rp":{"name":"NeuroComet"},"user":{"id":"AAAA","name":"dev","displayName":"dev"},"pubKeyCredParams":[],"timeout":60000,"attestation":"none"}"""
                            passkeyStatus = when (val r = PasskeyManager.registerPasskey(context, bogusChallenge)) {
                                is PasskeyResult.Success -> "✓ Registered (${r.responseJson.length} chars)"
                                is PasskeyResult.Failure -> "✗ Expected error: ${r.message.take(80)}"
                                else -> r.toString()
                            }
                        }
                    }) { Text("Register probe") }
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    passkeyStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

