package com.example.share_quote_app.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.example.share_quote_app.data.AuthResult

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun signIn(email: String, password: String): AuthResult<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error(e)
        }
    }

    suspend fun createUser(email: String, password: String): AuthResult<Boolean> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser() = firebaseAuth.currentUser
}
