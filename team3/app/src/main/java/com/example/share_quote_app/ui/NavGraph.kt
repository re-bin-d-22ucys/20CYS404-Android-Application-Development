package com.example.share_quote_app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.share_quote_app.ui.auth.LoginScreen
import com.example.share_quote_app.ui.auth.RegistrationScreen
import com.example.share_quote_app.viewModel.AuthViewModel
import com.example.share_quote_app.viewModel.FavoritesViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun NavGraph(authViewModel: AuthViewModel = viewModel(), favoritesViewModel: FavoritesViewModel = viewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.authEvent.collect {
            when (it) {
                is AuthViewModel.AuthEvent.SignOutSuccess -> {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
                else -> { /* Handle other events if necessary, or ignore */ }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "login") {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegistrationScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("main") {
            QuoteScreen(
                onSignOut = { authViewModel.signOut() },
                onNavigateToFavorites = { navController.navigate("favorites") }
            )
        }
        composable("favorites") {
            FavoritesScreen(onNavigateBack = { navController.popBackStack() }, favoritesViewModel = favoritesViewModel)
        }
    }
}