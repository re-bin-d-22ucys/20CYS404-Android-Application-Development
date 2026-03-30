package com.example.localweatherstation.models

data class ForecastItem(
    val dt_txt: String,           // Forecast date/time
    val main: Main,               // Temperature and humidity
    val weather: List<Weather>    // Weather description & icon
)
