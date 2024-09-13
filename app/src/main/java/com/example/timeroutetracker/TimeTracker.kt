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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
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
import com.example.timeroutetracker.utils.DayTimeSpan
import com.example.timeroutetracker.utils.TimeSpan
import com.example.timeroutetracker.utils.repeatIndefinitely
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
  }

  @Composable
  fun UsageStatsView() {
    val colorCycle = sequenceOf(
      Color.Red,
      Color.Blue,
      Color.Green,
      Color.Yellow,
      Color.Magenta,
      Color.Cyan
    ).repeatIndefinitely().iterator()
    val date = remember { mutableStateOf(LocalDate.now()) }
    val data = remember {
      mutableStateListOf<List<Pair<Long, Color>>>()
    }

    // 在每次 date 变化时更新 data
    LaunchedEffect(date.value) {
      Log.d(TAG, "update data from date: ${date.value}")
      data.clear()
      DayTimeSpan.fromLocalDate(date.value).splitTimeSpan().forEach { timeSpan ->
        val originData =
          getUsageStats(timeSpan)?.filter { it.value.totalTimeVisible > 0 } ?: mapOf()
        val thisHourData = mutableListOf<Pair<Long, Color>>()
        if (originData.isNotEmpty()) {
          originData.forEach { (packageName, usageStats) ->
            thisHourData.add(
              (usageStats.totalTimeVisible to colorCycle.next())
            )
          }
        }
        data.add(thisHourData)
      }
      assert(data.size == 24)
    }


//    getUsageStats(TimeSpan.today())?.values?.forEach { usageStat ->
//      Log.d(
//        "UsageStats",
//        "Package: ${usageStat.packageName}, Time: ${usageStat.totalTimeInForeground}"
//      )
//    }

    Scaffold(
      topBar = { DateHeader(date) },
    ) { innerPadding ->
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding()),
        color = MaterialTheme.colorScheme.background
      ) {
        BarChart(
          data = data,
          dataUpLimit = 60000L,
          modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
          xAxisBegin = 3,
          xAxisStep = 6,
          yAxisTransform = { it / 1000 }
        ) { index ->
          Log.i(TAG, "Bar $index clicked")
        }
      }
    }
  }

  @Composable
  fun UsageStatsViewExample() {
    val date = remember { mutableStateOf(LocalDate.now()) }
    Scaffold(
      topBar = { DateHeader(date) },
    ) { innerPadding ->
      // 示例数据：每个柱体由多个成分组成（占比和颜色）
      val data = listOf(
        listOf(120L to Color.Red, 200L to Color.Blue),
        listOf(210L to Color.Green, 200L to Color.Yellow),
        listOf(150L to Color.Green, 46L to Color.Yellow),
        listOf(450L to Color.Green),
        listOf(232L to Color.Green, 201L to Color.Yellow),
        listOf(95L to Color.Green, 31L to Color.Yellow),
        listOf(20L to Color.Magenta, 246L to Color.Cyan)
      )

      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding()),
        color = MaterialTheme.colorScheme.background
      ) {
        BarChart(
          data = data,
          dataUpLimit = 1000,
          modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
          xAxisBegin = 1,
          xAxisStep = 3
        ) { index ->
          Log.i(TAG, "Bar $index clicked")
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