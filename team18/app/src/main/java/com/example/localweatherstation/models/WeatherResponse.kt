package com.example.localweatherstation.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val name: String,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int, // Visibility in meters
    @SerializedName("uvi") val uvIndex: Float? // UV Index, nullable as it may not always be available
) {
    data class Main(
        @SerializedName("temp") val temp: Float,
        @SerializedName("temp_min") val tempMin: Float,
        @SerializedName("temp_max") val tempMax: Float,
        @SerializedName("feels_like") val feelsLike: Float,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("pressure") val pressure: Int
    )
    data class Weather(
        @SerializedName("description") val description: String,
        @SerializedName("icon") val iconCode: String
    )
    data class Wind(
        @SerializedName("speed") val speed: Float
    )
}