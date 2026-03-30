package com.example.share_quote_app

import kotlinx.serialization.Serializable

@Serializable
data class Quote(val q: String = "", val a: String = "")
