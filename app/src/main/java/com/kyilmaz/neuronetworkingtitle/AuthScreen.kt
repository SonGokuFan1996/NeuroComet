package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onVerify2FA: (String) -> Unit,
    is2FARequired: Boolean,
    error: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var twoFactorCode by remember { mutableStateOf("") }
    var isSignIn by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val titleText = if (is2FARequired) "Two-Factor Auth" else if (isSignIn) "Welcome Back!" else "Create Account"
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (is2FARequired) {
             OutlinedTextField(
                value = twoFactorCode,
                onValueChange = { twoFactorCode = it },
                label = { Text("Enter 2FA Code (123456)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onVerify2FA(twoFactorCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape
            ) {
                Text("Verify")
            }
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isSignIn) {
                        onSignIn(email, password)
                    } else {
                        onSignUp(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape
            ) {
                val btnText = if (isSignIn) "Sign In" else "Sign Up"
                Text(btnText)
            }

            TextButton(onClick = { isSignIn = !isSignIn }) {
                val linkText = if (isSignIn) "Don't have an account? Sign Up" else "Already have an account? Sign In"
                Text(linkText)
            }
        }
    }
}
