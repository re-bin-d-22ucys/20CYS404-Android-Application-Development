package com.example.localweatherstation.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Storm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.filled.LocationCity

// ------------------ Data Classes and Enums ------------------

data class CloudData(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val opacity: Float,
    val pulsePhase: Float
)

data class ParticleData(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val opacity: Float,
    val color: Color
)

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

data class SettingsSection(
    val title: String? = null,
    val items: List<SettingsItem>
)

data class CityItem(
    val name: String,
    val isCurrentLocation: Boolean = false
)

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

/** Defines the interval for displaying the 24-hour hourly forecast. **/
enum class ForecastDuration(val intervalHours: Int, val label: String) {
    ONE_HOUR_INTERVAL(1, "1 Hour Interval"), // 24 items
    TWO_HOUR_INTERVAL(2, "2 Hour Interval"); // 12 items

    /** The actual number of items displayed in the UI. **/
    val displayedItems: Int
        get() = 24 / intervalHours
}

// ------------------ Hourly Forecast Data Class ------------------

data class HourlyForecast(
    val time: LocalTime,
    val temp: Float,
    val iconCode: String,
    val description: String
)

/** Holds the complete weather data for a city. **/
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int = 10000,
    val uvIndex: Float? = null,
    val hourlyForecasts: List<HourlyForecast> = emptyList(),
    val sunrise: LocalTime = LocalTime.of(6, 11),
    val sunset: LocalTime = LocalTime.of(18, 8)
) {
    data class Main(
        val temp: Float,
        val tempMin: Float,
        val tempMax: Float,
        val feelsLike: Float,
        val humidity: Int,
        val pressure: Int
    )
    data class Weather(val description: String, val iconCode: String)
    data class Wind(val speed: Float)
}

// ------------------ Temperature Conversion Utilities ------------------

/** Converts Celsius to Fahrenheit. **/
fun celsiusToFahrenheit(celsius: Float): Float {
    return (celsius * 9f / 5f) + 32f
}

/** Formats temperature with unit (°C or °F). **/
fun formatTemperature(temp: Float, unit: TemperatureUnit): String {
    return when (unit) {
        TemperatureUnit.CELSIUS -> "${temp.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${celsiusToFahrenheit(temp).toInt()}°F"
    }
}

/** Formats temperature with unit (°C or °F) using the short format. **/
fun formatTemperatureShort(temp: Float, unit: TemperatureUnit): String {
    return when (unit) {
        TemperatureUnit.CELSIUS -> "${temp.toInt()}°C"
        TemperatureUnit.FAHRENHEIT -> "${celsiusToFahrenheit(temp).toInt()}°F"
    }
}

// ------------------ Wind Speed to Beaufort Scale Conversion ------------------

/** Converts wind speed (m/s) to Beaufort scale integer. **/
fun windSpeedToBeaufort(speed: Float): Int {
    return when {
        speed < 0.5 -> 0
        speed < 1.6 -> 1
        speed < 3.4 -> 2
        speed < 5.5 -> 3
        speed < 8.0 -> 4
        speed < 10.8 -> 5
        speed < 13.9 -> 6
        speed < 17.2 -> 7
        speed < 20.8 -> 8
        speed < 24.5 -> 9
        speed < 28.5 -> 10
        speed < 32.7 -> 11
        else -> 12
    }
}

// ------------------ Theme Functions ------------------

/** Provides a time-based gradient for the background. **/
@Composable
fun getThemeBasedGradient(isNight: Boolean): Brush {
    return if (isNight) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1C2526),
                Color(0xFF2C3E50),
                Color(0xFF4B5EAA),
                Color(0xFF6B728E),
                Color(0xFF3A506B),
                Color(0xFF1B263B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F4C75),
                Color(0xFF3282B8),
                Color(0xFF87CEEB),
                Color(0xFFBBE1FA),
                Color(0xFF64B5F6),
                Color(0xFF2196F3)
            )
        )
    }
}

// ------------------ Animated Background ------------------

/** Draws infinitely scrolling, pulsating cloud shapes. **/
@Composable
fun AnimatedClouds(modifier: Modifier = Modifier) {
    val clouds = remember {
        mutableStateListOf<CloudData>().apply {
            repeat(8) {
                add(
                    CloudData(
                        x = Random.nextFloat(),
                        y = Random.nextFloat() * 0.8f + 0.1f,
                        size = Random.nextFloat() * 60f + 25f,
                        speed = Random.nextFloat() * 0.5f + 0.2f,
                        opacity = Random.nextFloat() * 0.6f + 0.15f,
                        pulsePhase = Random.nextFloat() * 2f * PI.toFloat()
                    )
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_movement"
    )

    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val screenWidth = size.width
        val screenHeight = size.height

        clouds.forEach { cloud ->
            val currentX = (cloud.x * screenWidth + animationProgress * cloud.speed * screenWidth) % (screenWidth + 300f) - 150f
            val pulseFactor = 0.85f + 0.15f * sin(pulseProgress + cloud.pulsePhase)
            val dynamicSize = cloud.size * pulseFactor
            val dynamicOpacity = cloud.opacity * (0.7f + 0.3f * sin(pulseProgress + cloud.pulsePhase + 1f))

            drawEnhancedCloud(
                center = Offset(currentX, cloud.y * screenHeight),
                size = dynamicSize,
                opacity = dynamicOpacity
            )
        }
    }
}

/** Draws floating particles, suitable for night or misty effects. **/
@Composable
fun FloatingParticles(modifier: Modifier = Modifier) {
    val particles = remember {
        mutableStateListOf<ParticleData>().apply {
            repeat(15) {
                add(
                    ParticleData(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        size = Random.nextFloat() * 4f + 1f,
                        speed = Random.nextFloat() * 0.3f + 0.1f,
                        opacity = Random.nextFloat() * 0.4f + 0.1f,
                        color = if (Random.nextBoolean()) Color.White else Color.Cyan.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_movement"
    )

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_twinkle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val screenWidth = size.width
        val screenHeight = size.height

        particles.forEach { particle ->
            val currentY = (particle.y * screenHeight - particleProgress * particle.speed * screenHeight) % (screenHeight + 100f)
            val twinkleOpacity = particle.opacity * (0.3f + 0.7f * abs(sin(twinkle + particle.x * screenWidth * 0.01f)))

            drawCircle(
                color = particle.color.copy(alpha = twinkleOpacity),
                radius = particle.size,
                center = Offset(particle.x * screenWidth, currentY)
            )
        }
    }
}

/** Draws a stylized cloud shape composed of overlapping circles. **/
fun DrawScope.drawEnhancedCloud(center: Offset, size: Float, opacity: Float) {
    val cloudColor = Color.White.copy(alpha = opacity)

    drawCircle(color = cloudColor, radius = size * 0.7f, center = center)
    drawCircle(color = cloudColor, radius = size * 0.5f, center = Offset(center.x - size * 0.5f, center.y - size * 0.2f))
    drawCircle(color = cloudColor, radius = size * 0.6f, center = Offset(center.x + size * 0.6f, center.y - size * 0.1f))
    drawCircle(color = cloudColor, radius = size * 0.4f, center = Offset(center.x + size * 0.2f, center.y - size * 0.7f))
    drawCircle(color = cloudColor, radius = size * 0.35f, center = Offset(center.x - size * 0.3f, center.y - size * 0.6f))
}

// ------------------ Weather UI Elements ------------------

/** Displays the weather icon fetched asynchronously. **/
@Composable
fun StaticWeatherIcon(iconUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = iconUrl,
        contentDescription = "Weather Icon",
        modifier = modifier.size(120.dp),
        contentScale = ContentScale.Fit
    )
}

/** Displays the main current temperature. **/
@Composable
fun StaticTemperature(temperature: String, modifier: Modifier = Modifier) {
    Text(
        text = temperature,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 96.sp,
            color = Color.White
        ),
        modifier = modifier
    )
}

/** Displays a single weather detail with an icon and labels. **/
@Composable
fun WeatherDetailRow(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** A transparent, rounded card style for glassmorphism effect. **/
@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

// ------------------ Hourly Forecast Section ------------------

/**
 * Displays the hourly forecast, filtering the full 24-hour list
 * based on the selected `forecastDuration`.
 */
@Composable
fun HourlyForecastSection(
    hourlyForecasts: List<HourlyForecast>,
    temperatureUnit: TemperatureUnit,
    forecastDuration: ForecastDuration
) {
    val displayForecasts = remember(hourlyForecasts, forecastDuration) {
        if (hourlyForecasts.size < 24) {
            Log.w("HourlyForecastSection", "Hourly forecast list is shorter than 24 items (${hourlyForecasts.size}). Display may be incomplete.")
        }

        if (hourlyForecasts.isEmpty()) {
            emptyList()
        } else {
            val step = forecastDuration.intervalHours
            val maxItems = forecastDuration.displayedItems

            val indices = generateSequence(0) { it + step }
                .take(maxItems)
                .filter { it < hourlyForecasts.size }

            indices.map { index ->
                hourlyForecasts[index]
            }.toList()
        }
    }

    Log.d("HourlyForecastSection", "Displaying ${displayForecasts.size} items for ${forecastDuration.label}")

    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule Icon",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayForecasts) { forecast ->
                    HourlyForecastItem(forecast, unit = temperatureUnit)
                }
            }
        }
    }
}

/** Displays a single column item for the hourly forecast row. **/
@Composable
fun HourlyForecastItem(forecast: HourlyForecast, unit: TemperatureUnit) {
    val currentHour = LocalTime.now().truncatedTo(ChronoUnit.HOURS)
    val forecastTimeHour = forecast.time.truncatedTo(ChronoUnit.HOURS)

    val timeText = if (forecastTimeHour == currentHour) {
        "Now"
    } else {
        forecast.time.truncatedTo(ChronoUnit.MINUTES).toString()
    }

    val descriptionParts = forecast.description.split(" ")
    val descriptionText = descriptionParts.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: ""

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            )

            Text(
                text = descriptionText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            )
        }

        AsyncImage(
            model = "https://openweathermap.org/img/wn/${forecast.iconCode}@2x.png",
            contentDescription = forecast.description,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatTemperatureShort(forecast.temp, unit),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        )
    }
}

// ------------------ Sunrise/Sunset Component ------------------

/** Displays the Sunrise/Sunset card container. **/
@Composable
fun SunriseSunsetCard(sunriseTime: LocalTime, sunsetTime: LocalTime) {
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Sun Icon",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sunrise & Sunset",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SunriseSunsetArc(sunriseTime = sunriseTime, sunsetTime = sunsetTime)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Draws the arc representing the sun's path and positions an animated sun icon
 * which is visible only between sunrise and sunset.
 */
@Composable
fun SunriseSunsetArc(sunriseTime: LocalTime, sunsetTime: LocalTime) {
    val canvasHeight = 120.dp
    val sunIconSize = 24.dp
    val horizontalPadding = 32.dp

    val density = LocalDensity.current
    val sunIconSizePx = with(density) { sunIconSize.toPx() }
    val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }

    val now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
    val totalDaylightMinutes = ChronoUnit.MINUTES.between(sunriseTime, sunsetTime).toFloat()

    // ---------- THIS IS THE FIX ----------
    val progress = when {
        // State 1: Before sunrise (e.g., 00:00 - 06:10) -> 0%
        now.isBefore(sunriseTime) -> 0f

        // State 2: After sunset (e.g., 18:08 - 23:59) -> 0%
        now.compareTo(sunsetTime) >= 0 -> 0f // <-- CHANGED FROM 1f to 0f

        // State 3: Daytime (06:11 - 18:07) -> Calculate 0% to 100%
        else -> {
            val minutesSinceSunrise = ChronoUnit.MINUTES.between(sunriseTime, now).toFloat()
            // Ensure progress doesn't go over 1.0f due to edge cases
            (minutesSinceSunrise / totalDaylightMinutes).coerceIn(0f, 1f)
        }
    }
    // ------------------------------------

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "sun_path_progress"
    )

    // Sun Icon Visibility Logic
    // Use !isBefore to include the exact sunrise minute
    val isSunVisible = !now.isBefore(sunriseTime) && now.isBefore(sunsetTime)
    val sunAlphaTarget = if (isSunVisible) 1f else 0f

    val animatedSunAlpha by animateFloatAsState(
        targetValue = sunAlphaTarget,
        animationSpec = tween(durationMillis = 500),
        label = "sun_icon_alpha"
    )

    var sunXOffset by remember { mutableStateOf(0.dp) }
    var sunYOffset by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeight),
        contentAlignment = Alignment.TopStart
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
                .padding(vertical = 4.dp)
        ) {
            val width = size.width
            val height = size.height

            val arcWidth = width - 2 * horizontalPaddingPx
            val radius = arcWidth / 2f

            val arcCenter = Offset(width / 2f, height)

            val arcTopLeft = Offset(arcCenter.x - radius, arcCenter.y - radius)
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

            // Draw the full arc (semicircle) - the path of the sun
            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )

            // Draw the illuminated progress arc (yellow gradient)
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF9F871), Color(0xFFFF9472)),
                    start = arcTopLeft,
                    end = Offset(arcTopLeft.x + arcSize.width, arcTopLeft.y + arcSize.height)
                ),
                startAngle = 180f,
                sweepAngle = 180f * animatedProgress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
            )

            // Calculate Sun Icon Position
            val angleDegrees = 180f - (180f * animatedProgress)
            val angleRadians = angleDegrees * (PI / 180.0).toFloat()

            val sunXArcRelative = radius * kotlin.math.cos(angleRadians)
            val sunYArcRelative = radius * kotlin.math.sin(angleRadians)

            val sunXCenterPx = arcCenter.x + sunXArcRelative
            val sunYCenterPx = arcCenter.y - sunYArcRelative

            with(density) {
                sunXOffset = (sunXCenterPx - sunIconSizePx / 2f).toDp()
                sunYOffset = (sunYCenterPx - sunIconSizePx / 2f).toDp()
            }
        }

        // Animated Sun Icon
        Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = "Sun Position",
            tint = Color(0xFFFFE03B).copy(alpha = animatedSunAlpha),
            modifier = Modifier
                .size(sunIconSize)
                .offset(
                    x = sunXOffset,
                    y = sunYOffset
                )
        )
    }

    // Times Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding + 4.dp, top = 8.dp, end = horizontalPadding + 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sunrise Time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = "Sunrise",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = sunriseTime.truncatedTo(ChronoUnit.MINUTES).toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        // Sunset Time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = sunsetTime.truncatedTo(ChronoUnit.MINUTES).toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = "Sunset",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// ------------------ Enhanced Clickable Components ------------------

/** A bouncy, custom icon button with haptic feedback. **/
@Composable
fun EnhancedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Int = 28,
    tint: Color = Color.White
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "icon_scale"
    )

    IconButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .size(48.dp)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                },
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ------------------ Dialogs ------------------

/** Dialog for selecting the temperature unit. **/
@Composable
fun TemperatureUnitDialog(
    currentUnit: TemperatureUnit,
    onUnitSelected: (TemperatureUnit) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Temperature Unit",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUnitSelected(TemperatureUnit.CELSIUS)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentUnit == TemperatureUnit.CELSIUS,
                        onClick = {
                            onUnitSelected(TemperatureUnit.CELSIUS)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Celsius (°C)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUnitSelected(TemperatureUnit.FAHRENHEIT)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentUnit == TemperatureUnit.FAHRENHEIT,
                        onClick = {
                            onUnitSelected(TemperatureUnit.FAHRENHEIT)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fahrenheit (°F)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/** Dialog for selecting the forecast display interval. **/
@Composable
fun ForecastDurationDialog(
    currentDuration: ForecastDuration,
    onDurationSelected: (ForecastDuration) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Forecast Display Interval",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ForecastDuration.values().forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDurationSelected(duration)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentDuration == duration,
                            onClick = {
                                onDurationSelected(duration)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = duration.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

// ------------------ Enhanced Settings Components ------------------

/** A single row item for the Settings screen with dynamic dividers and click feedback. **/
@Composable
fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean = true,
    showArrow: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(150),
        label = "background_color"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable {
                    isPressed = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    item.onClick()
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            if (showArrow) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/** Header text for a section in the Settings screen. **/
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

// ------------------ City Search Screen ------------------

/** The screen for searching and selecting a new city or current location. **/
@Composable
fun CitySearchScreen(
    navController: NavController,
    onCitySelected: (String, Boolean) -> Unit
) {
    Log.d("CitySearchScreen", "Rendering CitySearchScreen")
    val currentTime = LocalTime.now()
    val isNight = currentTime.hour >= 18 || currentTime.hour < 6

    val cities = listOf(
        CityItem("Current Location", true),
        CityItem("Ambur"),
        CityItem("Bangalore"),
        CityItem("Chennai"),
        CityItem("Coimbatore"),
        CityItem("Cuddalore"),
        CityItem("Delhi"),
        CityItem("Dindigul"),
        CityItem("Erode"),
        CityItem("Ettimadai"),
        CityItem("Hosur"),
        CityItem("Hyderabad"),
        CityItem("Kanchipuram"),
        CityItem("Kanyakumari"),
        CityItem("Karur"),
        CityItem("Kochi"),
        CityItem("Kumbakonam"),
        CityItem("Madurai"),
        CityItem("Mumbai"),
        CityItem("Nagercoil"),
        CityItem("Palakkad"),
        CityItem("Pune"),
        CityItem("Salem"),
        CityItem("Thanjavur"),
        CityItem("Theni Allinagaram"),
        CityItem("Tiruchirappalli"),
        CityItem("Tirunelveli"),
        CityItem("Tiruppur"),
        CityItem("Vellore")
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = cities.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getThemeBasedGradient(isNight))
    ) {
        FloatingParticles()
        AnimatedClouds()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedIconButton(
                    onClick = {
                        try {
                            Log.d("CitySearchScreen", "Attempting to navigate back")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("CitySearchScreen", "Navigation back failed: ${e.localizedMessage}")
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )

                Text(
                    text = "Add City",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search for city weather",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (filteredCities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cities found",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredCities.size) { index ->
                            val cityItem = filteredCities[index]
                            var isPressed by remember { mutableStateOf(false) }
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                                animationSpec = tween(150),
                                label = "city_background"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .clickable {
                                        isPressed = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        try {
                                            Log.d("CitySearchScreen", "Selected city: ${cityItem.name}, isCurrentLocation: ${cityItem.isCurrentLocation}")
                                            onCitySelected(cityItem.name, cityItem.isCurrentLocation)
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            Log.e("CitySearchScreen", "City selection failed: ${e.localizedMessage}")
                                        }
                                    }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (cityItem.isCurrentLocation) Icons.Default.LocationOn else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = cityItem.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index < filteredCities.size - 1) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.2f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }

                            LaunchedEffect(isPressed) {
                                if (isPressed) {
                                    delay(100)
                                    isPressed = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ Enhanced Settings Screen ------------------

/** The main settings screen with options for units and forecast display. **/
@Composable
fun SettingsScreen(
    navController: NavController,
    temperatureUnit: TemperatureUnit,
    onTemperatureUnitChanged: (TemperatureUnit) -> Unit,
    forecastDuration: ForecastDuration,
    onForecastDurationChanged: (ForecastDuration) -> Unit
) {
    Log.d("SettingsScreen", "Rendering Enhanced SettingsScreen")
    val currentTime = LocalTime.now()
    val isNight = currentTime.hour >= 18 || currentTime.hour < 6

    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }

    val settingSections = remember(temperatureUnit, forecastDuration) {
        listOf(
            SettingsSection(
                title = "Home Screen Weather",
                items = listOf(
                    SettingsItem(
                        title = "Temperature unit",
                        subtitle = when (temperatureUnit) {
                            TemperatureUnit.CELSIUS -> "Celsius (°C)"
                            TemperatureUnit.FAHRENHEIT -> "Fahrenheit (°F)"
                        },
                        icon = Icons.Default.Thermostat,
                        onClick = {
                            Log.d("SettingsScreen", "Temperature unit clicked")
                            showTemperatureDialog = true
                        }
                    ),
                    SettingsItem(
                        title = "Weather forecast",
                        subtitle = forecastDuration.label,
                        icon = Icons.Default.Schedule,
                        onClick = {
                            Log.d("SettingsScreen", "Forecast duration clicked")
                            showDurationDialog = true
                        }
                    )
                )
            ),
            SettingsSection(
                title = "About",
                items = listOf(
                    SettingsItem(
                        title = "Version",
                        subtitle = "V1.0.0",
                        icon = Icons.Default.Info,
                        onClick = {
                            Log.d("SettingsScreen", "Version clicked")
                            // TODO: Show version details
                        }
                    )
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getThemeBasedGradient(isNight))
    ) {
        FloatingParticles()
        AnimatedClouds()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedIconButton(
                    onClick = {
                        try {
                            Log.d("SettingsScreen", "Attempting to navigate back")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("SettingsScreen", "Navigation back failed: ${e.localizedMessage}")
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    settingSections.forEach { section ->
                        section.title?.let { title ->
                            item {
                                SettingsSectionHeader(title)
                            }
                        }

                        items(section.items.size) { index ->
                            val item = section.items[index]
                            val isLastItem = index == section.items.size - 1
                            val isLastSection = section == settingSections.last()

                            SettingsItemRow(
                                item = item,
                                showDivider = !(isLastItem && isLastSection)
                            )
                        }

                        if (section != settingSections.last()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTemperatureDialog) {
        TemperatureUnitDialog(
            currentUnit = temperatureUnit,
            onUnitSelected = onTemperatureUnitChanged,
            onDismiss = { showTemperatureDialog = false }
        )
    }

    if (showDurationDialog) {
        ForecastDurationDialog(
            currentDuration = forecastDuration,
            onDurationSelected = onForecastDurationChanged,
            onDismiss = { showDurationDialog = false }
        )
    }
}

// ------------------ Main Screen ------------------

/** The main weather display screen. **/
@Composable
fun MainScreen(
    weatherData: WeatherResponse,
    scope: CoroutineScope,
    onRefresh: (Boolean) -> Unit,
    isRefreshing: Boolean,
    navController: NavController,
    isCurrentLocation: Boolean = false,
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    forecastDuration: ForecastDuration
) {
    Log.d("MainScreen", "Rendering MainScreen with city: ${weatherData.name}, isCurrentLocation: $isCurrentLocation, ForecastDuration: ${forecastDuration.label} (${forecastDuration.displayedItems} items)")
    val currentTime = LocalTime.now()
    val isNight = currentTime.hour >= 18 || currentTime.hour < 6

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { onRefresh(isCurrentLocation) },
        modifier = Modifier
            .fillMaxSize()
            .background(getThemeBasedGradient(isNight))
            .clipToBounds()
    ) {
        FloatingParticles()
        AnimatedClouds()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCurrentLocation) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Current Location",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = weatherData.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.End) {
                    EnhancedIconButton(
                        onClick = {
                            try {
                                Log.d("MainScreen", "Navigating to city_search")
                                navController.navigate("city_search")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation to city search failed: ${e.localizedMessage}")
                            }
                        },
                        // ICON CHANGE: Swapped Icons.Default.Add for Icons.Default.ViewList
                        icon = Icons.Default.LocationCity,
                        contentDescription = "Manage Cities",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    EnhancedIconButton(
                        onClick = {
                            try {
                                Log.d("MainScreen", "Navigating to settings")
                                navController.navigate("settings")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation to settings failed: ${e.localizedMessage}")
                            }
                        },
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Current Weather Display
            StaticTemperature(formatTemperatureShort(weatherData.main.temp, temperatureUnit))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = weatherData.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "N/A",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Min/FeelsLike/Max Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Min",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = formatTemperatureShort(weatherData.main.tempMin, temperatureUnit),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Feels like",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = formatTemperatureShort(weatherData.main.feelsLike, temperatureUnit),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Max",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = formatTemperatureShort(weatherData.main.tempMax, temperatureUnit),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weather Icon
            StaticWeatherIcon(
                iconUrl = "https://openweathermap.org/img/wn/${weatherData.weather.firstOrNull()?.iconCode ?: "01d"}@4x.png"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Hourly Forecast Section
            HourlyForecastSection(weatherData.hourlyForecasts, temperatureUnit, forecastDuration)
            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Detailed Weather Metrics
            GlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailRow(
                            label = "Humidity",
                            value = "${weatherData.main.humidity}%",
                            icon = Icons.Default.WaterDrop,
                            modifier = Modifier.weight(1f)
                        )
                        WeatherDetailRow(
                            label = "Pressure",
                            value = "${weatherData.main.pressure} hPa",
                            icon = Icons.Default.Air,
                            modifier = Modifier.weight(1f)
                        )
                        WeatherDetailRow(
                            label = "Wind Speed",
                            value = "${weatherData.wind.speed} m/s",
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailRow(
                            label = "Feels Like",
                            value = formatTemperatureShort(weatherData.main.feelsLike, temperatureUnit),
                            icon = Icons.Default.Thermostat,
                            modifier = Modifier.weight(1f)
                        )
                        WeatherDetailRow(
                            label = "Visibility",
                            value = "${weatherData.visibility / 1000} km",
                            icon = Icons.Default.Visibility,
                            modifier = Modifier.weight(1f)
                        )
                        WeatherDetailRow(
                            label = "Wind Force",
                            value = "${windSpeedToBeaufort(weatherData.wind.speed)}",
                            icon = Icons.Default.Storm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Card 3: Sunrise/Sunset Card (LAST CARD)
            SunriseSunsetCard(
                sunriseTime = weatherData.sunrise,
                sunsetTime = weatherData.sunset
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ------------------ Navigation Setup ------------------

/** The main entry point for the Weather App, managing navigation and state. **/
@Composable
fun WeatherApp(
    initialWeatherData: WeatherResponse,
    scope: CoroutineScope,
    fetchWeatherForCity: (String, CoroutineScope, (String) -> Unit, (Boolean) -> Unit, (WeatherResponse?) -> Unit) -> Unit,
    tryFetchLocation: (CoroutineScope, (String) -> Unit, (Boolean) -> Unit, (WeatherResponse?) -> Unit) -> Unit
) {
    val navController = rememberNavController()

    // Ensure the initial state ALWAYS contains a full 24-hour mock forecast
    val initialCity = initialWeatherData.name
    val initialDataWithMockForecasts = remember {
        initialWeatherData.copy(
            hourlyForecasts = generateCitySpecificMockHourlyForecasts(initialCity)
        )
    }

    var weatherData by remember { mutableStateOf(initialDataWithMockForecasts) }

    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isCurrentLocation by remember { mutableStateOf(true) }
    var selectedCity by remember { mutableStateOf(initialCity) }
    var temperatureUnit by remember { mutableStateOf(TemperatureUnit.CELSIUS) }
    var forecastDuration by remember { mutableStateOf(ForecastDuration.TWO_HOUR_INTERVAL) }

    Log.d("WeatherApp", "NavHost initialized with initial city: ${initialDataWithMockForecasts.name}")

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val onRefresh: (Boolean) -> Unit = { refreshAsCurrentLocation ->
                isRefreshing = true
                Log.d("WeatherApp", "Initiating refresh - isCurrentLocation: $refreshAsCurrentLocation, selectedCity: $selectedCity")

                val updateWeatherData: (WeatherResponse?) -> Unit = { newData ->
                    newData?.let {
                        weatherData = it.copy(
                            hourlyForecasts = generateCitySpecificMockHourlyForecasts(it.name)
                        )
                        Log.d("WeatherApp", "Refresh successful: ${it.name}")
                    } ?: Log.w("WeatherApp", "Refresh returned null data")
                    isRefreshing = false
                }

                val handleError: (String) -> Unit = { error ->
                    errorMessage = error
                    Log.e("WeatherApp", "Refresh error: $error")
                    isRefreshing = false
                }

                val handleLoading: (Boolean) -> Unit = { loading ->
                    isRefreshing = loading
                    Log.d("WeatherApp", "Refresh loading state: $loading")
                }

                if (refreshAsCurrentLocation) {
                    tryFetchLocation(scope, handleError, handleLoading, updateWeatherData)
                } else {
                    fetchWeatherForCity(selectedCity, scope, handleError, handleLoading, updateWeatherData)
                }
            }

            MainScreen(
                weatherData = weatherData,
                scope = scope,
                onRefresh = onRefresh,
                isRefreshing = isRefreshing,
                navController = navController,
                isCurrentLocation = isCurrentLocation,
                temperatureUnit = temperatureUnit,
                forecastDuration = forecastDuration
            )
        }
        composable("city_search") {
            CitySearchScreen(
                navController = navController,
                onCitySelected = { city, isCurrentLocationSelected ->
                    Log.d("WeatherApp", "Selected: $city, isCurrentLocation: $isCurrentLocationSelected")
                    isRefreshing = true
                    isCurrentLocation = isCurrentLocationSelected
                    selectedCity = if (isCurrentLocationSelected) "" else city

                    val updateWeatherData: (WeatherResponse?) -> Unit = { newData ->
                        newData?.let {
                            weatherData = it.copy(
                                hourlyForecasts = generateCitySpecificMockHourlyForecasts(it.name)
                            )
                            Log.d("WeatherApp", "Successfully updated weather data for: ${it.name}")
                        } ?: Log.w("WeatherApp", "Fetch returned null data for $city")
                        isRefreshing = false
                    }

                    val handleError: (String) -> Unit = { error ->
                        errorMessage = error
                        Log.e("WeatherApp", "Fetch error: $error")
                        isRefreshing = false
                    }

                    val handleLoading: (Boolean) -> Unit = { loading ->
                        isRefreshing = loading
                        Log.d("WeatherApp", "Fetch loading state: $loading")
                    }

                    if (isCurrentLocationSelected) {
                        tryFetchLocation(scope, handleError, handleLoading, updateWeatherData)
                    } else {
                        fetchWeatherForCity(city, scope, handleError, handleLoading, updateWeatherData)
                    }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                temperatureUnit = temperatureUnit,
                onTemperatureUnitChanged = { newUnit ->
                    temperatureUnit = newUnit
                    Log.d("WeatherApp", "Temperature unit changed to: $newUnit")
                },
                forecastDuration = forecastDuration,
                onForecastDurationChanged = { newDuration ->
                    forecastDuration = newDuration
                    Log.d("WeatherApp", "Forecast duration changed to: ${newDuration.label}")
                }
            )
        }
    }

    if (errorMessage.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            Log.e("WeatherApp", "Displaying error: $errorMessage")
            scope.launch {
                delay(3000)
                errorMessage = ""
                Log.d("WeatherApp", "Error message cleared")
            }
        }
    }
}

// ------------------ Mock Data Generator ------------------

/** Generates a full 24-hour mock hourly forecast list based on a city name seed. **/
fun generateCitySpecificMockHourlyForecasts(cityName: String): List<HourlyForecast> {
    val currentTime = LocalTime.now().truncatedTo(ChronoUnit.HOURS)

    val hoursToGenerate = 24
    val citySeed = cityName.hashCode().toLong()
    val random = Random(citySeed)

    return List(hoursToGenerate) { index ->
        val time = currentTime.plusHours(index.toLong())
        val isDay = time.hour in 6..17

        val baseTemp = (20f + (random.nextFloat() * 10f)) + sin(index * 0.7f) * 1.5f

        val weatherIndex = abs(citySeed.toInt() + index) % 6

        val (iconCode, description) = when (weatherIndex) {
            0, 5 -> Pair(if (isDay) "01d" else "01n", "Clear Sky")
            1, 4 -> Pair(if (isDay) "04d" else "04n", "Broken Clouds")
            2 -> Pair(if (isDay) "09d" else "09n", "Moderate Rain")
            else -> Pair(if (isDay) "03d" else "03n", "Scattered Clouds")
        }

        HourlyForecast(
            time = time,
            temp = baseTemp,
            iconCode = iconCode,
            description = description
        )
    }
}

// ------------------ Preview ------------------

@Preview(showBackground = true, name = "Main Screen with Hourly Forecast")
@Composable
fun MainScreenHourlyForecastPreview() {
    val initialCityName = "Ettimadai"
    val mockHourlyData = generateCitySpecificMockHourlyForecasts(initialCityName)

    val fixedSunrise = LocalTime.of(6, 11)
    val fixedSunset = LocalTime.of(18, 8)

    WeatherApp(
        initialWeatherData = WeatherResponse(
            name = initialCityName,
            main = WeatherResponse.Main(
                temp = 28f,
                tempMin = 26f,
                tempMax = 30f,
                feelsLike = 29f,
                humidity = 63,
                pressure = 1010
            ),
            weather = listOf(
                WeatherResponse.Weather(description = "overcast clouds", iconCode = "04d")
            ),
            wind = WeatherResponse.Wind(speed = 2.04f),
            visibility = 10000,
            uvIndex = 5.5f,
            hourlyForecasts = mockHourlyData,
            sunrise = fixedSunrise,
            sunset = fixedSunset
        ),
        scope = rememberCoroutineScope(),
        fetchWeatherForCity = { _, _, _, _, _ -> },
        tryFetchLocation = { _, _, _, _ -> }
    )
}

@Preview(showBackground = true, name = "City Search Screen")
@Composable
fun CitySearchScreenPreview() {
    CitySearchScreen(
        navController = rememberNavController(),
        onCitySelected = { _, _ -> }
    )
}

@Preview(showBackground = true, name = "Settings Screen")
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        navController = rememberNavController(),
        temperatureUnit = TemperatureUnit.CELSIUS,
        onTemperatureUnitChanged = { },
        forecastDuration = ForecastDuration.TWO_HOUR_INTERVAL,
        onForecastDurationChanged = {}
    )
}