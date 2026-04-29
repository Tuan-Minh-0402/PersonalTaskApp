package com.example.personaltaskapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.personaltaskapp.repository.AuthManager

@Composable
fun RegisterScreen(
    authManager: AuthManager,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        error?.let {
            Text(it, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                when {
                    trimmedEmail.isEmpty() -> error = "Email is required"
                    !trimmedEmail.contains("@") -> error = "Email is invalid"
                    password.length < 6 -> error = "Password must be at least 6 characters"
                    password != confirmPassword -> error = "Confirm password does not match"
                    authManager.emailExists(trimmedEmail) -> error = "Email already exists"
                    else -> {
                        error = null
                        authManager.register(trimmedEmail, password)
                            .onSuccess {
                                authManager.login(trimmedEmail, password)
                                onRegisterSuccess()
                            }
                            .onFailure { error = it.message ?: "Register failed" }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Sign Up")
        }

        Text(
            text = "Already have account? Login",
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable { onGoToLogin() }
        )
    }
}