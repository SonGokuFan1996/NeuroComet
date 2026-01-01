package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Reusable dialog for age verification. Returns an [Audience] derived from the entered age.
 */
@Composable
fun AgeVerificationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Audience) -> Unit,
    onSkip: (() -> Unit)? = {},
    title: String = "Verify your age",
    subtitle: String = "We use your age to set the right safety mode.",
    initialAge: String = ""
) {
    var ageText by remember { mutableStateOf(TextFieldValue(initialAge)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(subtitle)
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { newValue ->
                        ageText = newValue
                        error = null
                    },
                    label = { Text("Your age") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(text = error.orEmpty(), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val age = ageText.text.trim().toIntOrNull()
                if (age == null || age <= 0) {
                    error = "Enter a valid age"
                    return@Button
                }
                val audience = audienceForAge(age)
                onConfirm(audience)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = {
                    onSkip?.invoke()
                    onDismiss()
                }) { Text("Skip for now") }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

fun audienceForAge(age: Int): Audience = when {
    age < 13 -> Audience.UNDER_13
    age < 18 -> Audience.TEEN
    else -> Audience.ADULT
}
