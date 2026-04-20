# WeatherMood 🌦️

WeatherMood is a smart Android application that suggests the perfect places to visit based on your current (or projected) weather "mood." Whether it's a sunny day for a park or a stormy evening for a cozy spa, WeatherMood helps you plan your day with precision.

## ✨ Key Features

- **Real-Time Weather & Mood**: Fetches current conditions and translates them into a "Mood" (Adventure, Culture, Cozy, etc.).
- **Precise 5-Day Planning**: Select any date and time in **30-minute intervals** within the next 5 days to see projected weather and appropriate recommendations.
- **Smart 50km Fallback**: If no specific mood-matching places are found within your immediate 1.5km area, the app automatically expands its search to a **50km radius**.
- **Unified Navigation**: One-click "Navigate with Google Maps" that draws a road directly from your active location (Current or Picked) to your destination.
- **Persistent State**: Your selected location, date, and hour are saved as you navigate between tabs.
- **Local Favorites**: Save and manage your favorite spots using an offline-first local database.
- **Security First**: Forced manual sign-in on every app launch to ensure account-specific data privacy.

## 🛠️ Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (ViewModel, LiveData, activityViewModels)
- **Networking**: Retrofit 2 + Moshi (for OpenWeather and Google Places APIs)
- **Database**: Room (for local data persistence)
- **UI/UX**: DataBinding, ViewBinding, Material 3, Glide (Image loading)
- **Maps**: Google Maps SDK & Google Places SDK
- **Backend**: Node.js + Express + MongoDB (handles auth & password reset codes)

## 🚀 Setup & Requirements

To run this project locally, you must configure the environment and provide your own API keys.

### 1. API Keys
You need to create a `local.properties` file in the **project root** and add the following:

```properties
# OpenWeather API Key (https://openweathermap.org/api)
OPENWEATHER_API_KEY=your_openweather_key_here

# Google Maps & Places API Key (https://console.cloud.google.com/)
GOOGLE_PLACES_API_KEY=your_google_key_here

# Backend Base URL (Use your computer's local IP for physical phone testing)
BACKEND_BASE_URL=http://192.168.x.x:3000
```

### 2. Backend Configuration
Navigate to the `/backend` folder and create a `.env` file:

```env
MONGO_URI=your_mongodb_connection_string
EMAIL_USER=your_gmail_address@gmail.com
EMAIL_PASS=your_gmail_app_password
```

### 3. Running the Project
1. **Backend**: 
   - `cd backend`
   - `npm install`
   - `npm run dev`
2. **Android**: 
   - Open the project in **Android Studio Ladybug** or newer.
   - Sync Gradle.
   - Ensure your phone/emulator is on the same Wi-Fi as your computer.
   - Click **Run**.

## 🌦️ Weather Source
Weather data is provided by [OpenWeather](https://openweathermap.org/), using the Current Weather and 5-Day/3-Hour Forecast endpoints.
