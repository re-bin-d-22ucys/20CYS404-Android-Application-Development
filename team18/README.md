# Weather-Station  
A beginner-friendly Android application that fetches and displays **real-time weather data** for selected cities using GPS, Web APIs, and modern Android development practices.

---

## Project Overview
**Weather-Station** is an Android app designed to provide up-to-date weather conditions for your current location as well as any chosen city.

The project demonstrates how to combine **location services**, **runtime permissions**, **REST APIs**, and **JSON parsing** in a real mobile application.

---

## Team Members

| Name | Register Number |
|------|------------------|
| **Krishnamoorthi P L** | CB.EN.U4CYS22033 |
| **Sree Sharvesh S S** | CB.EN.U4CYS22061 |
| **Mukesh R** | CB.EN.U4CYS22068 |
| **Vishal S** | CB.EN.U4CYS22075 |

---

## Project Goal
To build an Android application capable of:

- Fetch device location using Google Play Services  
- Get the weather data from OpenWeatherMap API  
- Parse JSON responses into readable weather metrics  
- Displays the data with a clean, modern UI  
- Allows city selection and customization (units, intervals, etc.)

---

## Key Technical Concepts

### 1. Google Play Services (Location)
- Uses **FusedLocationProvider** for efficient, battery-friendly location access  
- Retrieves precise **latitude & longitude** for weather requests  

### 2. Runtime Permissions
- Handles **fine** and **coarse** location permissions  
- Gracefully manages user denial  
- Ensures privacy compliance with modern Android standards  

### 3. Connecting Web APIs
- Integrates **OpenWeatherMap API** (Current Weather endpoint)  
- Sends HTTP GET requests using **city name** or **coordinates**  
- Requires a developer API key (free on OpenWeatherMap)  

**Example Request:**  `api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY`

### 4. JSON Parsing
- Converts JSON responses into Kotlin/Java model objects    
- Extracts temperature, humidity, wind speed, weather descriptions, etc.

---

## Weather Data Flow
1. **Get Location** – Retrieve GPS coordinates  
2. **Request Weather** – Send request to OpenWeatherMap  
3. **Process JSON** – Parse weather details  
4. **Display UI** – Update screens with weather data  

---

## App Features & Screens

### Main Weather Display
- Current temperature  
- Weather description  
- Min/Max temperature  
- Hourly forecast  
- Automatic theme switch (Daylight → Dark Mode)

### Multiple Locations
- Search interface for cities  
- Predefined city list  
- "Current Location" quick access  

### Settings
- Temperature units: **°C** or **°F**  
- Forecast update interval: **1 hour / 2 hours**  
- App info and versioning  

### Fahrenheit & Celsius Modes
- Full UI adapts to selected temperature unit  

### Dark Theme Mode
- Automatically activates after 6 PM  
- Lower eye strain  

### Error Handling
- Detects disabled GPS/location services  
- Displays clean, minimal error page with suggested actions  

