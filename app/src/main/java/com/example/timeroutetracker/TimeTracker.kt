package com.example.timeroutetracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.example.timeroutetracker.components.PermissionManager
import com.example.timeroutetracker.utils.TimeSpan

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
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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