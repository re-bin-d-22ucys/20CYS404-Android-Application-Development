package com.example.share_quote_app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth

import com.example.share_quote_app.data.AuthResult
import com.example.share_quote_app.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    val authRepository = AuthRepository()

    private val _isLoggedIn = MutableStateFlow(authRepository.getCurrentUser() != null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _authEventChannel = Channel<AuthEvent>()
    val authEvent = _authEventChannel.receiveAsFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success<Boolean> -> {
                    _isLoggedIn.value = true
                    _authEventChannel.send(AuthEvent.LoginSuccess)
                }
                is AuthResult.Error -> {
                    _isLoggedIn.value = false
                    _authEventChannel.send(AuthEvent.Error(result.exception.message ?: "Unknown login error"))
                }
            }
        }
    }

    fun createUser(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.createUser(email, password)) {
                is AuthResult.Success<Boolean> -> {
                    _isLoggedIn.value = true
                    _authEventChannel.send(AuthEvent.RegistrationSuccess)
                }
                is AuthResult.Error -> {
                    _isLoggedIn.value = false
                    _authEventChannel.send(AuthEvent.Error(result.exception.message ?: "Unknown registration error"))
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _isLoggedIn.value = false
        viewModelScope.launch {
            _authEventChannel.send(AuthEvent.SignOutSuccess)
        }
    }

    sealed class AuthEvent {
        object LoginSuccess : AuthEvent()
        object RegistrationSuccess : AuthEvent()
        object SignOutSuccess : AuthEvent()
        data class Error(val message: String) : AuthEvent()
    }
}
