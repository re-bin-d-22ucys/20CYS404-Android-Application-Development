package com.example.share_quote_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.share_quote_app.viewModel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import com.example.share_quote_app.ui.theme.NeonBlue
import com.example.share_quote_app.ui.theme.NeonGreen

@Composable
fun RegistrationScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatchError by remember { mutableStateOf(false) }
    var showPasswords by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.authEvent.collect {
            when (it) {
                is AuthViewModel.AuthEvent.RegistrationSuccess -> {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
                is AuthViewModel.AuthEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(it.message ?: "Unknown error")
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = Color.Black) { paddingValues ->
        AuthScreenContent {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NeonBlue,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = NeonBlue
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NeonBlue,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = NeonBlue
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = NeonBlue,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = NeonBlue
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showPasswords,
                    onCheckedChange = { showPasswords = it },
                    colors = CheckboxDefaults.colors(checkedColor = NeonGreen, uncheckedColor = Color.Gray)
                )
                Text("Show Passwords", color = Color.White)
            }
            if (passwordMismatchError) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password == confirmPassword) {
                        passwordMismatchError = false
                        authViewModel.createUser(email, password)
                    } else {
                        passwordMismatchError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Register", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
            ) {
                Text("Go to Login", color = Color.Black)
            }
        }
    }
}