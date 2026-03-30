package com.example.share_quote_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.authEvent.collect {
            when (it) {
                is AuthViewModel.AuthEvent.LoginSuccess -> {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                is AuthViewModel.AuthEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Your email/password is wrong, please try again")
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
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                        )
                    }
                },
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
            Button(
                onClick = { authViewModel.signIn(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Login", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
            ) {
                Text("Go to Register", color = Color.Black)
            }
        }
    }
}