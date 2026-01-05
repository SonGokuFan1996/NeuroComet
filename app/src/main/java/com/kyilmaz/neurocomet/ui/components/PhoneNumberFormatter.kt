package com.kyilmaz.neurocomet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation that formats phone numbers as user types.
 * Supports multiple format styles:
 * - US: (XXX) XXX-XXXX
 * - International: +X XXX XXX XXXX
 * - Simple: XXX-XXX-XXXX
 *
 * Neurodivergent-friendly features:
 * - Clear visual grouping reduces cognitive load
 * - Predictable formatting pattern
 * - Numbers are easier to verify when formatted
 */
class PhoneNumberVisualTransformation(
    private val format: PhoneFormat = PhoneFormat.US
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val formatted = formatPhoneNumber(digits, format)

        return TransformedText(
            AnnotatedString(formatted),
            PhoneNumberOffsetMapping(digits, formatted, format)
        )
    }

    private fun formatPhoneNumber(digits: String, format: PhoneFormat): String {
        return when (format) {
            PhoneFormat.US -> formatUS(digits)
            PhoneFormat.INTERNATIONAL -> formatInternational(digits)
            PhoneFormat.SIMPLE -> formatSimple(digits)
            PhoneFormat.UK -> formatUK(digits)
            PhoneFormat.GERMAN -> formatGerman(digits)
            PhoneFormat.FRENCH -> formatFrench(digits)
            PhoneFormat.JAPANESE -> formatJapanese(digits)
            PhoneFormat.AUSTRALIAN -> formatAustralian(digits)
            PhoneFormat.INDIAN -> formatIndian(digits)
            PhoneFormat.BRAZILIAN -> formatBrazilian(digits)
            PhoneFormat.TURKISH -> formatTurkish(digits)
            PhoneFormat.KOREAN -> formatKorean(digits)
        }
    }

    private fun formatUS(digits: String): String {
        // Format: (XXX) XXX-XXXX
        return buildString {
            digits.take(10).forEachIndexed { index, char ->
                when (index) {
                    0 -> append("($char")
                    2 -> append("$char) ")
                    5 -> append("$char-")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatInternational(digits: String): String {
        // Format: +X XXX XXX XXXX (generic international)
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    1, 4, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatSimple(digits: String): String {
        // Format: XXX-XXX-XXXX
        return buildString {
            digits.take(10).forEachIndexed { index, char ->
                when (index) {
                    3, 6 -> append("-$char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatUK(digits: String): String {
        // Format: XXXXX XXXXXX
        return buildString {
            digits.take(11).forEachIndexed { index, char ->
                when (index) {
                    5 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatGerman(digits: String): String {
        // Format: +49 XXX XXXXXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 5 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatFrench(digits: String): String {
        // Format: +33 X XX XX XX XX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(11).forEachIndexed { index, char ->
                when (index) {
                    2, 3, 5, 7, 9 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatJapanese(digits: String): String {
        // Format: +81 XX-XXXX-XXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" $char")
                    4, 8 -> append("-$char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatAustralian(digits: String): String {
        // Format: +61 X XXXX XXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(11).forEachIndexed { index, char ->
                when (index) {
                    2, 3, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatIndian(digits: String): String {
        // Format: +91 XXXXX XXXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatBrazilian(digits: String): String {
        // Format: +55 (XX) XXXXX-XXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(13).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" ($char")
                    4 -> append(") $char")
                    9 -> append("-$char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatTurkish(digits: String): String {
        // Format: +90 XXX XXX XX XX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 5, 8, 10 -> append(" $char")
                    else -> append(char)
                }
            }
        }
    }

    private fun formatKorean(digits: String): String {
        // Format: +82 XX-XXXX-XXXX
        return buildString {
            if (digits.isNotEmpty()) append("+")
            digits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" $char")
                    4, 8 -> append("-$char")
                    else -> append(char)
                }
            }
        }
    }
}

enum class PhoneFormat {
    US,           // (XXX) XXX-XXXX
    INTERNATIONAL, // +X XXX XXX XXXX
    SIMPLE,       // XXX-XXX-XXXX
    UK,           // XXXXX XXXXXX
    GERMAN,       // +49 XXX XXXXXXX
    FRENCH,       // +33 X XX XX XX XX
    JAPANESE,     // +81 XX-XXXX-XXXX
    AUSTRALIAN,   // +61 X XXXX XXXX
    INDIAN,       // +91 XXXXX XXXXX
    BRAZILIAN,    // +55 (XX) XXXXX-XXXX
    TURKISH,      // +90 XXX XXX XX XX
    KOREAN        // +82 XX-XXXX-XXXX
}

/**
 * Offset mapping for phone number formatting.
 * Maps cursor positions between original and formatted text.
 */
private class PhoneNumberOffsetMapping(
    private val original: String,
    private val formatted: String,
    private val format: PhoneFormat
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        // Calculate position in formatted string based on digit count
        val digitsBeforeOffset = original.take(offset).count { it.isDigit() }
        var formattedIndex = 0
        var digitCount = 0

        for (char in formatted) {
            if (digitCount >= digitsBeforeOffset) break
            formattedIndex++
            if (char.isDigit()) digitCount++
        }

        return formattedIndex.coerceAtMost(formatted.length)
    }

    override fun transformedToOriginal(offset: Int): Int {
        // Count digits up to the offset position
        return formatted.take(offset).count { it.isDigit() }.coerceAtMost(original.length)
    }
}

/**
 * Convenience composable for a phone number text field with auto-formatting.
 *
 * @param value The raw phone number (digits only)
 * @param onValueChange Called with the raw digits when user types
 * @param format The phone format style to use
 * @param label Label for the text field
 * @param placeholder Placeholder text
 * @param modifier Modifier for the text field
 */
@Composable
fun PhoneNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    format: PhoneFormat = PhoneFormat.US,
    label: @Composable (() -> Unit)? = { Text("Phone Number") },
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Filled.Phone, contentDescription = null) },
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    val maxLength = when (format) {
        PhoneFormat.US -> 10
        PhoneFormat.INTERNATIONAL -> 12
        PhoneFormat.SIMPLE -> 10
        PhoneFormat.UK -> 11
        PhoneFormat.GERMAN -> 12
        PhoneFormat.FRENCH -> 11
        PhoneFormat.JAPANESE -> 12
        PhoneFormat.AUSTRALIAN -> 11
        PhoneFormat.INDIAN -> 12
        PhoneFormat.BRAZILIAN -> 13
        PhoneFormat.TURKISH -> 12
        PhoneFormat.KOREAN -> 12
    }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only keep digits and limit to max length
            val digits = newValue.filter { it.isDigit() }.take(maxLength)
            onValueChange(digits)
        },
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = PhoneNumberVisualTransformation(format),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}

/**
 * Utility function to format a raw phone number string.
 */
fun formatPhoneNumber(digits: String, format: PhoneFormat = PhoneFormat.US): String {
    val cleanDigits = digits.filter { it.isDigit() }
    return when (format) {
        PhoneFormat.US -> buildString {
            cleanDigits.take(10).forEachIndexed { index, char ->
                when (index) {
                    0 -> append("($char")
                    2 -> append("$char) ")
                    5 -> append("$char-")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.INTERNATIONAL -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    1, 4, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.SIMPLE -> buildString {
            cleanDigits.take(10).forEachIndexed { index, char ->
                when (index) {
                    3, 6 -> append("-$char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.UK -> buildString {
            cleanDigits.take(11).forEachIndexed { index, char ->
                when (index) {
                    5 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.GERMAN -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 5 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.FRENCH -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(11).forEachIndexed { index, char ->
                when (index) {
                    2, 3, 5, 7, 9 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.JAPANESE -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" $char")
                    4, 8 -> append("-$char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.AUSTRALIAN -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(11).forEachIndexed { index, char ->
                when (index) {
                    2, 3, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.INDIAN -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 7 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.BRAZILIAN -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(13).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" ($char")
                    4 -> append(") $char")
                    9 -> append("-$char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.TURKISH -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2, 5, 8, 10 -> append(" $char")
                    else -> append(char)
                }
            }
        }
        PhoneFormat.KOREAN -> buildString {
            if (cleanDigits.isNotEmpty()) append("+")
            cleanDigits.take(12).forEachIndexed { index, char ->
                when (index) {
                    2 -> append(" $char")
                    4, 8 -> append("-$char")
                    else -> append(char)
                }
            }
        }
    }
}

