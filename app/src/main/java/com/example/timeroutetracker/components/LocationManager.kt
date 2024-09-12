package com.example.timeroutetracker.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng


class LocationManager(
  private val activity: ComponentActivity,
  private val interval: Long,
  private val callback: (Location) -> Unit
) {

  private val locationCallback: LocationCallback

  private val fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(activity)


  private val requestPermissionLauncher =
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        startLocationUpdates()
      }
    }

  init {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        val last = locationResult.lastLocation ?: return
        callback(last)
      }
    }

    if (ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
//      startLocationUpdates()
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }

  fun getLocation(): LatLng? {
    if (ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val last = fusedLocationClient.lastLocation.result
      return LatLng(last.latitude, last.longitude)
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    return null
  }

  /*
   * DO NOT USE THIS:
   * java.lang.IllegalStateException: MainActivity is attempting to register while current state is RESUMED. LifecycleOwners must call register before they are STARTED.
   */
  fun startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(
        activity,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(500)
        .setMaxUpdateDelayMillis(2000)
        .setIntervalMillis(interval)
        .build()
      Log.i("MyTest", "startLocationUpdates: $locationRequest")
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
      )
    } else {
      Log.w("MyTest", "failed to start location updates")
    }
  }

  fun stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }
}
