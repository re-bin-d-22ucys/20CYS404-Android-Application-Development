package com.example.share_quote_app.data.repository

import com.example.share_quote_app.Quote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class FavoritesRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUser
        get() = auth.currentUser

    suspend fun addFavorite(quote: Quote) {
        currentUser?.let {
            firestore.collection("users").document(it.uid).collection("favorites").add(quote).await()
        }
    }

    suspend fun removeFavorite(quote: Quote) {
        currentUser?.let {
            val result = firestore.collection("users").document(it.uid).collection("favorites")
                .whereEqualTo("q", quote.q)
                .whereEqualTo("a", quote.a)
                .get()
                .await()
            for (document in result.documents) {
                document.reference.delete().await()
            }
        }
    }

    suspend fun getFavorites(): List<Quote> {
        return currentUser?.let { user ->
            Log.d("FavoritesRepo", "Fetching favorites for user: ${user.uid}")
            val result = firestore.collection("users").document(user.uid).collection("favorites").get().await()
            Log.d("FavoritesRepo", "Found ${result.documents.size} favorite documents.")
            for (document in result.documents) {
                Log.d("FavoritesRepo", "Document data: ${document.data}")
            }
            val favoritesList = result.toObjects(Quote::class.java)
            Log.d("FavoritesRepo", "Converted to ${favoritesList.size} Quote objects: $favoritesList")
            favoritesList
        } ?: run {
            Log.d("FavoritesRepo", "No current user, returning empty list.")
            emptyList()
        }
    }

    suspend fun isFavorite(quote: Quote): Boolean {
        return currentUser?.let {
            val result = firestore.collection("users").document(it.uid).collection("favorites")
                .whereEqualTo("q", quote.q)
                .whereEqualTo("a", quote.a)
                .get()
                .await()
            !result.isEmpty
        } ?: false
    }
}
