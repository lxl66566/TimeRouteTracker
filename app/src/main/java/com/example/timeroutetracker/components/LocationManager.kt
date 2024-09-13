package com.example.timeroutetracker.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

private const val TAG = "LocationManager"

class LocationManager(
  private val fragment: Fragment,
  private val interval: Long,
  private var callback: ((Location) -> Unit)? = null
) {

  private val locationCallback: LocationCallback
  private val fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(fragment.requireContext())

  private val requestPermissionLauncher =
    fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        startLocationUpdates()
      }
    }

  init {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        val last = locationResult.lastLocation ?: return
        callback?.let { it(last) }
      }
    }

    if (ContextCompat.checkSelfPermission(
        fragment.requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      // Uncomment if needed
      startLocationUpdates()
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }

  // TODO: Do not use this! crash
  fun getLocation(): LatLng? {
    if (ContextCompat.checkSelfPermission(
        fragment.requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val last = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
      last.addOnSuccessListener { Log.i(TAG, "getLocation: $it") }
      last.addOnFailureListener { Log.e(TAG, "getLocation: $it") }
      last.addOnCompleteListener { Log.i(TAG, "getLocation: $it") }


//      val last = fusedLocationClient.lastLocation.result
//      return LatLng(last.latitude, last.longitude)
    } else {
      requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    return null
  }

  fun startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(
        fragment.requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(500)
        .setMaxUpdateDelayMillis(2000)
        .setIntervalMillis(interval)
        .build()
      Log.i(TAG, "startLocationUpdates: $locationRequest")
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        fragment.requireActivity().mainLooper
      )
    } else {
      Log.w(TAG, "failed to start location updates")
    }
  }

  fun stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }

  fun setCallback(callback: ((Location) -> Unit)?) {
    this.callback = callback
  }
}
