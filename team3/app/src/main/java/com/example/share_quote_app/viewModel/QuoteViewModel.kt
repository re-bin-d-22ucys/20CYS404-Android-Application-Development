package com.example.share_quote_app.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.share_quote_app.Quote
import com.example.share_quote_app.QuoteManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuoteViewModel : ViewModel() {

    private val _quoteList = MutableStateFlow<List<Quote>>(emptyList())
    val quoteList = _quoteList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _currentQuoteIndex = MutableStateFlow(0)
    val currentQuoteIndex = _currentQuoteIndex.asStateFlow()

    init {
        loadQuotes()
    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.value = true
            val quotes = QuoteManager.loadQuotesFromApi()
            _quoteList.value = quotes ?: listOf(Quote("Failed to fetch quotes.", "System"))
            _isLoading.value = false
            Log.d("QuoteViewModel", "Quotes loaded: ${_quoteList.value.size}")
        }
    }

    fun updateCurrentQuoteIndex(index: Int) {
        _currentQuoteIndex.value = index
    }
}
