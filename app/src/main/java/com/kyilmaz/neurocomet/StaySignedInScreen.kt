package com.kyilmaz.neurocomet

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

/**
 * Manages the "Stay Signed In" preference persistence.
 * Similar to Microsoft's authentication flow, this allows users to
 * choose whether to remain signed in on the device.
 */
object StaySignedInSettings {
    private const val PREFS_AUTH = "auth_prefs"
    private const val KEY_STAY_SIGNED_IN = "stay_signed_in"
    private const val KEY_DONT_SHOW_AGAIN = "stay_signed_in_dont_show"
    private const val KEY_SHOWN_ONCE = "stay_signed_in_shown"

    /**
     * Check if the Stay Signed In prompt should be displayed.
     * Returns true only if user hasn't chosen "Don't show again" and hasn't seen it yet this session.
     */
    fun shouldShowPrompt(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_DONT_SHOW_AGAIN, false) &&
               !prefs.getBoolean(KEY_SHOWN_ONCE, false)
    }

    /**
     * Check if the user has previously chosen to stay signed in.
     */
    fun isStaySignedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STAY_SIGNED_IN, false)
    }

    /**
     * Save the user's preference for staying signed in.
     */
    fun savePreference(context: Context, staySignedIn: Boolean, dontShowAgain: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_STAY_SIGNED_IN, staySignedIn)
            putBoolean(KEY_DONT_SHOW_AGAIN, dontShowAgain)
            putBoolean(KEY_SHOWN_ONCE, true)
        }
    }

    /**
     * Mark that the prompt has been shown for this session.
     */
    fun markAsShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOWN_ONCE, true) }
    }

    /**
     * Reset the "shown" flag for testing purposes (dev only).
     */
    fun resetShownFlag(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_SHOWN_ONCE, false)
            putBoolean(KEY_DONT_SHOW_AGAIN, false)
        }
    }

    /**
     * Clear all stay signed in preferences (used on sign out).
     */
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_STAY_SIGNED_IN)
            remove(KEY_DONT_SHOW_AGAIN)
            remove(KEY_SHOWN_ONCE)
        }
    }
}

/**
 * Microsoft-style "Stay Signed In" prompt screen.
 *
 * Displayed after successful authentication to ask users if they want
 * to remain signed in on this device. This reduces the frequency of
 * sign-in prompts while giving users control over their session persistence.
 *
 * @param userEmail The email of the signed-in user (displayed for context)
 * @param userDisplayName Optional display name to show instead of email
 * @param onYes Called when user chooses to stay signed in
 * @param onNo Called when user chooses not to stay signed in
 */
@Composable
fun StaySignedInScreen(
    userEmail: String = "",
    userDisplayName: String? = null,
    onYes: (dontShowAgain: Boolean) -> Unit,
    onNo: (dontShowAgain: Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // User Avatar Circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display name or email
            if (!userDisplayName.isNullOrBlank()) {
                Text(
                    text = userDisplayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (userEmail.isNotBlank()) {
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Main question
            Text(
                text = "Stay signed in?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "What does this mean?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Select \"Yes\" to reduce the number of times you're asked to sign in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Select \"No\" if this is a shared device or you prefer to sign in each time for extra security.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trust warning
            Text(
                text = "⚠️ Only do this on a device you trust",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onNo(dontShowAgain) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "No",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onYes(dontShowAgain) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Yes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Don't show again checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { dontShowAgain = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "Don't ask me again",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Privacy note
            Text(
                text = "Your session data is encrypted and stored securely on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

