package com.example.share_quote_app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.share_quote_app.Quote
import com.example.share_quote_app.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val favoritesRepository = FavoritesRepository()

    private val _favorites = MutableStateFlow<List<Quote>>(emptyList())
    val favorites = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = favoritesRepository.getFavorites()
        }
    }

    fun addFavorite(quote: Quote) {
        viewModelScope.launch {
            favoritesRepository.addFavorite(quote)
            loadFavorites()
        }
    }

    fun removeFavorite(quote: Quote) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(quote)
            loadFavorites()
        }
    }

    fun isFavorite(quote: Quote, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            callback(favoritesRepository.isFavorite(quote))
        }
    }
}
