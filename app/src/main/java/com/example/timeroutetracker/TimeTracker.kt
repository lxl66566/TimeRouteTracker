package com.example.timeroutetracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timeroutetracker.components.PermissionManager
import com.example.timeroutetracker.utils.TimeSpan
import com.example.timeroutetracker.utils.linearRatio

class TimeTracker(private val context: Context) {
  private final val permManager = PermissionManager(context)

  init {
    permManager.tryRequestUsageStatsPermission()
  }

  /*
   * Returns the usage stats for the given time span
   */
  fun getUsageStats(span: TimeSpan): MutableMap<String, UsageStats>? {
    if (!permManager.hasUsageStatsPermission()) return null
    val usageStatsManager =
      context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val currentTime = System.currentTimeMillis()
    val stats = usageStatsManager.queryAndAggregateUsageStats(
      span.startInMillis(),
      span.endInMillis()
    )
    return stats
//    // 处理获取的数据
//    stats?.forEach { usageStat ->
//      Log.d("UsageStats", "Package: ${usageStat.packageName}, Time: ${usageStat.totalTimeInForeground}")
//    }
  }

}

