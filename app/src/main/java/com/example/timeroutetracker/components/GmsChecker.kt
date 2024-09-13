package com.example.timeroutetracker.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GoogleApiAvailabilityLight


// 常量定义
private const val REQUEST_GOOGLE_PLAY_SERVICES = 9000

private const val TAG = "GmsChecker"

class GmsChecker {

  companion object {
    fun checkGooglePlayServicesAvailable(context: Context): Int {
      val googleApiAvailability = GoogleApiAvailabilityLight.getInstance()
      val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

      if (resultCode == ConnectionResult.SUCCESS) {
        // Google Play Services is available.
        Log.i(TAG, "Google Play Services is available.")
      } else {
        // Google Play Services is not available or up-to-date.
        if (googleApiAvailability.isUserResolvableError(resultCode)) {
          // An error message will be displayed if Google Play Services is not up-to-date.
          Log.e(TAG, "Google Play Services is not available. Showing error dialog.")

          // Display error dialog

          // The dialog that's displayed when Google Play Services is not available
          // or up-to-date will be shown here.

          // If Google Play Services can be installed
          // but isn't up-to-date, ask the user to update
          // Google Play Services on their device.

          GoogleApiAvailability.getInstance()
            .getErrorDialog(context as Activity, resultCode, REQUEST_GOOGLE_PLAY_SERVICES)?.show();
        } else {
          // The error is not recoverable, display a message to the user.
          Log.e(TAG, "Google Play Services is not available. The error is not recoverable")
        }
      }
      return resultCode
    }
  }
}