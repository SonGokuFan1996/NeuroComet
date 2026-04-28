package com.kyilmaz.neurocomet

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Developer diagnostics for the account-lifecycle flows required by Google
 * Play Data Safety:
 *
 *  - Schedule / cancel account deletion (14-day grace window)
 *  - Start / end detox mode
 *  - Read back `AccountLifecycleStatus` from Supabase
 *  - Count rows belonging to the signed-in user across the main tables so we
 *    can verify a deletion RPC actually cascaded
 *
 * Every destructive action is guarded with a confirmation dialog so an
 * accidental tap doesn't wipe the current session. All operations run
 * against the REAL Supabase backend — nothing is mocked — so this is how you
 * verify production behaviour before submitting to Play.
 */
@Composable
fun AccountLifecycleDevSection(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by authViewModel.user.collectAsState()
    val status by authViewModel.accountStatus.collectAsState()
    val pending by authViewModel.pendingAccountAction.collectAsState()

    var lastResultOk by remember { mutableStateOf<Boolean?>(null) }
    var lastResultMessage by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    var rowCounts by remember { mutableStateOf<Map<String, Int>?>(null) }
    var countingRows by remember { mutableStateOf(false) }

    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }
    var detoxDays by remember { mutableStateOf("1") }

    LaunchedEffect(user?.id) {
        if (user != null) authViewModel.refreshCurrentAccountStatus()
    }

    DevSectionCard(title = "Account Lifecycle", icon = Icons.Filled.ManageAccounts) {

        // ── Current account summary ──────────────────────────
        Text("Signed-in user", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        KV("User ID", user?.id ?: "<none>")
        KV("Display", user?.name ?: "<none>")
        KV("Verified", (user?.isVerified == true).toString())
        if (user?.id == "guest_user") {
            Spacer(Modifier.height(4.dp))
            WarnRow("Guest session — sign in with a real Supabase account to exercise deletion/detox flows.")
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── Server-side lifecycle status ─────────────────────
        Text("Lifecycle status (from Supabase `users` row)", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        val st = status
        if (st == null) {
            Text("No status yet — tap Refresh.", style = MaterialTheme.typography.bodySmall)
        } else {
            KV("is_active", st.is_active.toString())
            KV("deletion_scheduled_at", st.deletion_scheduled_at ?: "null")
            KV("detox_started_at", st.detox_started_at ?: "null")
            KV("detox_until", st.detox_until ?: "null")
            KV("hasDeletionScheduled", st.hasDeletionScheduled.toString())
            KV("isDetoxActive", st.isDetoxActive.toString())
        }
        if (pending != null) {
            Spacer(Modifier.height(4.dp))
            InfoRow("Pending action: ${pending!!::class.simpleName}")
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = !busy && user != null,
                onClick = {
                    scope.launch {
                        busy = true
                        authViewModel.refreshCurrentAccountStatus()
                        busy = false
                    }
                }
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Refresh status")
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── Deletion ─────────────────────────────────────────
        Text("Account deletion", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Schedules soft-delete 14 days out, marks is_active=false, and signs you out. " +
                "Sign back in within 14 days and Cancel to restore.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !busy && user != null && user?.id != "guest_user",
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { confirmAction = ConfirmAction.ScheduleDeletion }
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Schedule deletion")
            }
            OutlinedButton(
                enabled = !busy && user != null && status?.hasDeletionScheduled == true,
                onClick = { confirmAction = ConfirmAction.CancelDeletion }
            ) {
                Icon(Icons.Filled.Cancel, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Cancel deletion")
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── Detox ─────────────────────────────────────────────
        Text("Detox mode", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Pauses the account for N days and signs you out. End it manually from another signed-in session " +
                "or let it expire.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = detoxDays,
            onValueChange = { detoxDays = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("Detox days") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !busy && user != null && user?.id != "guest_user",
                onClick = { confirmAction = ConfirmAction.StartDetox(detoxDays.toIntOrNull() ?: 1) }
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Start detox")
            }
            OutlinedButton(
                enabled = !busy && user != null && status?.isDetoxActive == true,
                onClick = { confirmAction = ConfirmAction.EndDetox }
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("End detox")
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── Data-cascade verification ───────────────────────
        Text("Data cascade verification", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Counts the signed-in user's rows across core tables. After a hard-delete runs server-side, " +
                "every count must be 0 to satisfy Play Data Safety 'data deleted on request'.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(
            enabled = !countingRows && user != null && user?.id != "guest_user",
            onClick = {
                val uid = user?.id ?: return@Button
                countingRows = true
                scope.launch {
                    rowCounts = countUserRows(uid)
                    countingRows = false
                }
            }
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (countingRows) "Counting…" else "Count my rows")
        }
        rowCounts?.let { counts ->
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(Modifier.padding(10.dp)) {
                    counts.forEach { (table, n) ->
                        val status = when {
                            n < 0 -> LifecycleStatus.WARN to "query failed"
                            n == 0 -> LifecycleStatus.OK to "0 rows"
                            else -> LifecycleStatus.INFO to "$n row${if (n == 1) "" else "s"}"
                        }
                        StatusLine(status.first, table, status.second)
                    }
                }
            }
        }

        // ── Last operation result ────────────────────────────
        lastResultOk?.let { ok ->
            Spacer(Modifier.height(12.dp))
            if (ok) OkRow(lastResultMessage) else ErrRow(lastResultMessage)
        }
    }

    // ── Confirmation dialog ──────────────────────────────────
    confirmAction?.let { action ->
        val (title, body, confirm) = when (action) {
            is ConfirmAction.ScheduleDeletion -> Triple(
                "Schedule account deletion?",
                "This marks your account inactive and queues a hard delete in 14 days. You will be signed out. Sign back in and Cancel within the window to restore.",
                "Schedule"
            )
            is ConfirmAction.CancelDeletion -> Triple(
                "Cancel scheduled deletion?",
                "Removes the scheduled deletion and reactivates your account.",
                "Cancel deletion"
            )
            is ConfirmAction.StartDetox -> Triple(
                "Start detox for ${action.days} day(s)?",
                "Pauses your account and signs you out. You will not receive notifications during this period.",
                "Start detox"
            )
            is ConfirmAction.EndDetox -> Triple(
                "End detox now?",
                "Clears detox timestamps so you can sign back in immediately.",
                "End detox"
            )
        }
        AlertDialog(
            onDismissRequest = { if (!busy) confirmAction = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        val cb: (Boolean, String) -> Unit = { ok, msg ->
                            lastResultOk = ok
                            lastResultMessage = msg
                            busy = false
                            confirmAction = null
                        }
                        when (action) {
                            is ConfirmAction.ScheduleDeletion -> authViewModel.scheduleAccountDeletion(cb)
                            is ConfirmAction.CancelDeletion -> authViewModel.cancelScheduledDeletion(cb)
                            is ConfirmAction.StartDetox -> authViewModel.startDetoxMode(action.days, cb)
                            is ConfirmAction.EndDetox -> authViewModel.endDetoxMode(cb)
                        }
                    }
                ) { Text(confirm) }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { confirmAction = null }) { Text("Abort") }
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────────

private sealed interface ConfirmAction {
    data object ScheduleDeletion : ConfirmAction
    data object CancelDeletion : ConfirmAction
    data class StartDetox(val days: Int) : ConfirmAction
    data object EndDetox : ConfirmAction
}

private enum class LifecycleStatus { OK, WARN, FAIL, INFO }

@Composable
private fun KV(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(k, modifier = Modifier.width(160.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(v, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun InfoRow(text: String) = StatusLine(LifecycleStatus.INFO, text, "")

@Composable
private fun OkRow(text: String) = StatusLine(LifecycleStatus.OK, text, "")

@Composable
private fun ErrRow(text: String) = StatusLine(LifecycleStatus.FAIL, text, "")

@Composable
private fun WarnRow(text: String) = StatusLine(LifecycleStatus.WARN, text, "")

@Composable
private fun StatusLine(s: LifecycleStatus, title: String, detail: String) {
    val (icon, tint) = when (s) {
        LifecycleStatus.OK -> Icons.Filled.CheckCircle to Color(0xFF2E7D32)
        LifecycleStatus.WARN -> Icons.Filled.Warning to Color(0xFFEF6C00)
        LifecycleStatus.FAIL -> Icons.Filled.Block to MaterialTheme.colorScheme.error
        LifecycleStatus.INFO -> Icons.Filled.Info to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (detail.isNotBlank()) {
                Text(detail, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Counts the signed-in user's rows across the main tables. Returns -1 for
 * any table whose query fails (e.g. RLS denied or table missing) so the UI
 * can show a warning rather than crashing.
 */
private suspend fun countUserRows(userId: String): Map<String, Int> {
    val client = AppSupabaseClient.client ?: return mapOf("<supabase unavailable>" to -1)
    // Tables + the column holding this user's foreign key. Additions are safe;
    // if a table doesn't exist in this environment we just log -1.
    val targets = linkedMapOf(
        "users" to "id",
        "posts" to "user_id",
        "comments" to "user_id",
        "likes" to "user_id",
        "bookmarks" to "user_id",
        "follows" to "follower_id",
        "direct_messages" to "sender_id",
        "conversations" to "created_by",
        "notifications" to "user_id",
        "reports" to "reporter_id"
    )
    return targets.mapValues { (table, col) ->
        runCatching {
            client.from(table)
                .select(columns = Columns.list(col)) {
                    filter { eq(col, userId) }
                }
                .decodeList<Map<String, String?>>()
                .size
        }.getOrDefault(-1)
    }
}

