package com.example.localweatherstation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.localweatherstation.ui.WeatherApp
import com.example.localweatherstation.ui.WeatherResponse as UiWeatherResponse
import com.example.localweatherstation.ui.HourlyForecast
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
// NOTE: Assuming BuildConfig and WeatherService (Retrofit setup) are correctly defined elsewhere.
// NOTE: The models package must contain the API response data classes.

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var weatherService: WeatherService
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            weatherService = WeatherService.create()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize services: ${e.localizedMessage}")
            setContent {
                MaterialTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Initialization error. Please restart the app.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            return
        }

        val permissionResultState = mutableStateOf<Boolean?>(null)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            permissionResultState.value = fineGranted || coarseGranted
        }

        // MOCK HOURLY FORECAST GENERATOR
        fun generateCitySpecificMockHourlyForecasts(cityName: String): List<HourlyForecast> {
            val currentTime = LocalTime.now().truncatedTo(ChronoUnit.HOURS)
            val citySeed = cityName.hashCode().toLong()
            val random = Random(citySeed)

            return List(6) { index ->
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

        // Helper function to convert the model's WeatherResponse (from API)
        // to the UI's augmented WeatherResponse (which includes forecasts).
        fun com.example.localweatherstation.models.WeatherResponse.toUiWeatherResponse(
            cityName: String,
            forecasts: List<HourlyForecast>? = null
        ): UiWeatherResponse {
            return UiWeatherResponse(
                name = this.name,
                main = UiWeatherResponse.Main(
                    temp = this.main.temp,
                    tempMin = this.main.tempMin,
                    tempMax = this.main.tempMax,
                    feelsLike = this.main.feelsLike,
                    humidity = this.main.humidity,
                    pressure = this.main.pressure
                ),
                weather = this.weather.map { UiWeatherResponse.Weather(it.description, it.iconCode) },
                wind = UiWeatherResponse.Wind(this.wind.speed),
                visibility = this.visibility,
                uvIndex = this.uvIndex,
                hourlyForecasts = forecasts ?: generateCitySpecificMockHourlyForecasts(cityName)
            )
        }


        fun fetchWeather(
            lat: Double? = null,
            lon: Double? = null,
            city: String? = null,
            scope: CoroutineScope,
            setWeatherData: (UiWeatherResponse?) -> Unit,
            setErrorMessage: (String) -> Unit,
            setIsLoading: (Boolean) -> Unit
        ) {
            scope.launch {
                setIsLoading(true)
                try {
                    val modelResponse = when {
                        city != null -> weatherService.getCurrentWeatherByCity(
                            city = city,
                            apiKey = BuildConfig.WEATHER_API_KEY
                        )
                        lat != null && lon != null -> weatherService.getCurrentWeatherByCoordinates(
                            lat = lat,
                            lon = lon,
                            apiKey = BuildConfig.WEATHER_API_KEY
                        )
                        else -> throw IllegalArgumentException("Either city or coordinates must be provided")
                    }

                    val cityNameForMock = city ?: modelResponse.name

                    val uiResponse = modelResponse.toUiWeatherResponse(cityNameForMock)

                    setWeatherData(uiResponse)
                    setErrorMessage("")
                } catch (e: Exception) {
                    Log.e("WeatherFetch", "Failed: ${e.localizedMessage}")
                    val errorMessage = when (e) {
                        is HttpException -> {
                            when (e.code()) {
                                404 -> "City not found. Please check the city name."
                                401 -> "Invalid API key. Please contact support."
                                else -> "Failed to fetch weather: ${e.message()}"
                            }
                        }
                        is IOException -> "Network error. Please check your connection."
                        else -> "Failed to fetch weather: ${e.localizedMessage}"
                    }
                    setErrorMessage(errorMessage)
                }
                setIsLoading(false)
            }
        }

        fun tryFetchLocation(
            scope: CoroutineScope,
            setErrorMessage: (String) -> Unit,
            setIsLoading: (Boolean) -> Unit,
            setWeatherData: (UiWeatherResponse?) -> Unit,
            city: String? = null
        ) {
            if (city != null) {
                fetchWeather(
                    city = city,
                    scope = scope,
                    setWeatherData = setWeatherData,
                    setErrorMessage = setErrorMessage,
                    setIsLoading = setIsLoading
                )
                return
            }

            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                ) {
                    setErrorMessage("Location services are disabled. Please enable GPS or network location.")
                    setIsLoading(false)
                    return
                }

                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            fetchWeather(
                                lat = location.latitude,
                                lon = location.longitude,
                                scope = scope,
                                setWeatherData = setWeatherData,
                                setErrorMessage = setErrorMessage,
                                setIsLoading = setIsLoading
                            )
                        } else {
                            val locationRequest = LocationRequest.Builder(
                                Priority.PRIORITY_HIGH_ACCURACY, 10000
                            ).setMinUpdateIntervalMillis(5000).build()

                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult) {
                                    result.lastLocation?.let {
                                        fetchWeather(
                                            lat = it.latitude,
                                            lon = it.longitude,
                                            scope = scope,
                                            setWeatherData = setWeatherData,
                                            setErrorMessage = setErrorMessage,
                                            setIsLoading = setIsLoading
                                        )
                                        fusedLocationClient.removeLocationUpdates(this)
                                    }
                                }
                            }
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback!!,
                                Looper.getMainLooper()
                            )
                        }
                    }.addOnFailureListener { e ->
                        setErrorMessage("Failed to get location: ${e.localizedMessage}")
                        setIsLoading(false)
                    }
                } else {
                    setErrorMessage("Location permission not granted.")
                    setIsLoading(false)
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            } catch (e: Exception) {
                setErrorMessage("Error fetching location: ${e.localizedMessage}")
                setIsLoading(false)
            }
        }

        setContent {
            val currentTime = LocalTime.now()
            val isNight = currentTime.hour >= 18 || currentTime.hour < 6
            val colors = if (isNight) nightLightColors() else dayLightColors()

            MaterialTheme(
                colorScheme = colors,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes
            ) {
                var weatherData by remember { mutableStateOf<UiWeatherResponse?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf("") }

                val scope = rememberCoroutineScope()

                // Initial fetch and permission handling
                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        tryFetchLocation(
                            scope = scope,
                            setErrorMessage = { errorMessage = it },
                            setIsLoading = { isLoading = it },
                            setWeatherData = { weatherData = it }
                        )
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                // Handle permission result
                LaunchedEffect(permissionResultState.value) {
                    if (permissionResultState.value == true) {
                        tryFetchLocation(
                            scope = scope,
                            setErrorMessage = { errorMessage = it },
                            setIsLoading = { isLoading = it },
                            setWeatherData = { weatherData = it }
                        )
                    } else if (permissionResultState.value == false) {
                        errorMessage = "Location permission not granted."
                        isLoading = false
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        ) {
                            errorMessage = "Location permission denied permanently. Please enable it in app settings."
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        errorMessage.isNotEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    if (errorMessage.contains("permission", true)) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = {
                                            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                                                !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                                            ) {
                                                errorMessage = "Please enable location permission in app settings."
                                            } else {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        }) {
                                            Text("Request Permission Again")
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            weatherData?.let {
                                WeatherApp(
                                    initialWeatherData = it,
                                    scope = scope,
                                    fetchWeatherForCity = { city, scope, setErrorMessage, setIsLoading, setWeatherData ->
                                        fetchWeather(
                                            city = city,
                                            scope = scope,
                                            setWeatherData = setWeatherData,
                                            setErrorMessage = setErrorMessage,
                                            setIsLoading = setIsLoading
                                        )
                                    },
                                    tryFetchLocation = { scope, setErrorMessage, setIsLoading, setWeatherData ->
                                        tryFetchLocation(
                                            scope = scope,
                                            setErrorMessage = setErrorMessage,
                                            setIsLoading = setIsLoading,
                                            setWeatherData = setWeatherData
                                        )
                                    }
                                )

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to remove location updates: ${e.localizedMessage}")
        }
    }
}

@Composable
private fun dayLightColors() = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
private fun nightLightColors() = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White
)