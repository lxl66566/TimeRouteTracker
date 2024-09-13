package com.example.timeroutetracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.example.timeroutetracker.components.BarChart
import com.example.timeroutetracker.components.PermissionManager
import com.example.timeroutetracker.database.DB
import com.example.timeroutetracker.utils.TimeSpan
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "TimeTracker"

class TimeTracker(
  private val context: Context,
  private val fragmentManager: FragmentManager,
  private val db: DB,
  private val settings: Settings
) {
  private val permManager = PermissionManager(context)
  val dateRangePicker =
    MaterialDatePicker.Builder
      .dateRangePicker()
      .build()

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

  @Composable
  fun UsageStatsView() {
    val date = remember { mutableStateOf(LocalDate.now()) }
    Scaffold(
      topBar = { DateHeader(date) },
    ) { innerPadding ->
      // 示例数据：每个柱体由多个成分组成（占比和颜色）
      val data = listOf(
        listOf(0.4f to Color.Red, 0.1f to Color.Blue),
        listOf(0.2f to Color.Green, 0.4f to Color.Yellow),
        listOf(0.3f to Color.Green, 0.3f to Color.Yellow),
        listOf(0.3f to Color.Green, 0.2f to Color.Yellow),
        listOf(0.3f to Color.Green, 0.5f to Color.Yellow),
        listOf(0.15f to Color.Green, 0.4f to Color.Yellow),
        listOf(0.4f to Color.Magenta, 0.5f to Color.Cyan)
      )

      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding()),
        color = MaterialTheme.colorScheme.background
      ) {
        BarChart(
          data = data,
          modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
          xAxisBegin = 1,
          xAxisStep = 3
        ) { index ->
          Log.i("Test", "Bar $index clicked")
        }
      }
    }
  }


  @Composable
  fun DateHeader(
    selectedDate: MutableState<LocalDate>,
    onPreviousDate: (LocalDate) -> Unit = {},
    onNextDate: (LocalDate) -> Unit = {},
    onDateRangePicked: (LocalDate, LocalDate) -> Unit = { _, _ -> }
  ) {
    Row(
      modifier = Modifier
        .height(40.dp)
        .padding(1.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left button
      IconButton(onClick = {
        Log.d(TAG, "Previous date: $selectedDate")
        selectedDate.value = selectedDate.value.minusDays(1)
        onPreviousDate(selectedDate.value)
      }) {
        Icon(
          imageVector = Icons.Filled.ChevronLeft,
          contentDescription = "Previous"
        )
      }

      // Date selector box
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp)
          .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
          .padding(8.dp)
          .clickable {
            // Pick date range
            PickDateRange(onDateRangePicked)
          },
      ) {
        Text(
          modifier = Modifier
            .align(Alignment.Center),
          text = selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center
        )
      }

      // Right button
      IconButton(onClick = {
        Log.d(TAG, "Next date: $selectedDate")
        selectedDate.value = selectedDate.value.plusDays(1)
        onNextDate(selectedDate.value)
      }) {
        Icon(
          imageVector = Icons.Filled.ChevronRight,
          contentDescription = "Next"
        )
      }

    }
  }


  private fun PickDateRange(onDateRangePicked: (LocalDate, LocalDate) -> Unit) {
    dateRangePicker.show(fragmentManager, "time_range_picker")
    dateRangePicker.addOnPositiveButtonClickListener {
      val temp = convertToLocalDatePair(it)
      Log.d(TAG, "Date range picked: $temp")
      onDateRangePicked(temp.first, temp.second)
    }
  }
}


fun convertToLocalDatePair(pair: androidx.core.util.Pair<Long, Long>): Pair<LocalDate, LocalDate> {
  val zoneId = ZoneId.systemDefault()
  val startDate = Instant.ofEpochMilli(pair.first).atZone(zoneId).toLocalDate()
  val endDate = Instant.ofEpochMilli(pair.second).atZone(zoneId).toLocalDate()
  return Pair(startDate, endDate)
}