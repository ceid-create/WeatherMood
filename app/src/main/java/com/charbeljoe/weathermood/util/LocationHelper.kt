package com.charbeljoe.weathermood.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(context: Context, onResult: (lat: Double, lon: Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        fusedClient.getCurrentLocation(request, null).addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            } else {
                fusedClient.lastLocation.addOnSuccessListener { last ->
                    if (last != null) {
                        onResult(last.latitude, last.longitude)
                    } else {
                        requestFreshLocation(onResult)
                    }
                }
            }
        }
    }

    private fun requestFreshLocation(onResult: (lat: Double, lon: Double) -> Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedClient.removeLocationUpdates(this)
                result.lastLocation?.let { onResult(it.latitude, it.longitude) }
            }
        }
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }
}
