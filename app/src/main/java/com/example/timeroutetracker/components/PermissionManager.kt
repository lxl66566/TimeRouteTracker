package com.example.timeroutetracker.components

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

val PermissionTag = "Permission"

class PermissionManager(private val context: Context) {
  /*
   * Returns true if the app has permission to access usage stats
   */
  fun hasUsageStatsPermission(): Boolean {
    val appOpsManager = this.context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOpsManager.unsafeCheckOpNoThrow(
      AppOpsManager.OPSTR_GET_USAGE_STATS,
      android.os.Process.myUid(),
      this.context.packageName
    )
    val result = mode == AppOpsManager.MODE_ALLOWED
    Log.d(PermissionTag, "check Usage stats permission: $result")
    return result
  }

  /*
   * Open settings to requests permission to access usage stats
   */
  fun requestUsageStatsPermission() {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    this.context.startActivity(intent)
  }

  fun tryRequestUsageStatsPermission() {
    if (!hasUsageStatsPermission()) {
      requestUsageStatsPermission()
    }
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  fun requestWritePermission() {
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_MEDIA_IMAGES
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
        1001
      )
    }
  }
}