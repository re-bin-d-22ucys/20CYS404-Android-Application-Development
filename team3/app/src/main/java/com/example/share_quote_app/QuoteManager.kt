package com.example.share_quote_app

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object QuoteManager {

    suspend fun loadQuotesFromApi(): List<Quote>? {
        return withContext(Dispatchers.IO) {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                    })
                }
            }
            try {
                client.get("https://zenquotes.io/api/quotes").body<List<Quote>>()
            } catch (e: Exception) {
                Log.e("QuoteManager", "Error loading quotes: ${e.message}", e)
                null
            } finally {
                client.close()
            }
        }
    }
}