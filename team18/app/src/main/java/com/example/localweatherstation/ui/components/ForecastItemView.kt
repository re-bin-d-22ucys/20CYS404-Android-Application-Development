package com.example.localweatherstation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localweatherstation.models.ForecastItem

@Composable
fun ForecastItemView(item: ForecastItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.dt_txt,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Temp: ${item.main.temp}°C",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Condition: ${item.weather[0].description}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
