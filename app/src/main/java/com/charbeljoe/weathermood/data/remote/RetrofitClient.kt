package com.charbeljoe.weathermood.data.remote

import com.charbeljoe.weathermood.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// This object creates the 3 APIs the app uses: Weather API, Place API and the backend API
object RetrofitClient {

    // Uses Moshi (JSON converter)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // connects to OpenWeather API
    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    // connects to Places API
    val placesApi: PlacesApiService by lazy { //by lazy => don’t create it until it’s first used, then reuse it
        Retrofit.Builder() //Creates a builder for Retrofit
            .baseUrl("https://places.googleapis.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi)) //Uses moshi to convert data from JSON to kotlin or vice versa
            .build() // Finalizes the configuration
            .create(PlacesApiService::class.java) //Create the API implementation
    }

    //Backend api (server)
    val backendApi: BackendApiService by lazy {
        val baseUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/') + "/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApiService::class.java)
    }
}
