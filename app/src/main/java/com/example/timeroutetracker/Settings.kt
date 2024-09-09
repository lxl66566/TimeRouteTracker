package com.example.timeroutetracker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import com.example.timeroutetracker.database.DB
import com.example.timeroutetracker.utils.Base


class Settings(private val db: DB) {
  companion object {
    const val SETTINGS_TABLE_NAME = "settings"
    const val BACKGROUND_ROUTE = "Track route in background"
    const val SAMPLE_RATE_ROUTE = "GPS sample interval"
  }

  private val kvTable = db.kvTable(SETTINGS_TABLE_NAME)
  private val groupPaddingValues = PaddingValues(8.dp)

  @Composable
  fun TotalSettings() {
    val state = rememberLazyListState()
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
      state = state
    ) {
      item {
        TimeSettings()
        RouteSettings()
        OtherSettings()
      }
    }
  }

  @Composable
  fun TimeSettings() {
    SettingsGroup(
      modifier = Modifier,
      title = { Text("Time Tracker") },
      contentPadding = groupPaddingValues,
      enabled = true
    ) {
      SettingsMenuLink(title = { Text("Change category") }) { TODO() }
    }
  }

  @Composable
  fun RouteSettings() {
    SettingsGroup(
      modifier = Modifier,
      title = { Text("Route") },
      contentPadding = groupPaddingValues,
      enabled = true
    ) {
      var trackBackgroundState by remember {
        mutableStateOf(
          kvTable.getOrInitAny(
            BACKGROUND_ROUTE,
            defaultValue = true
          )
        )
      }
      var sampleRateState by remember {
        mutableStateOf(
          kvTable.getOrInitAny(
            SAMPLE_RATE_ROUTE,
            1.0f
          )
        )
      }

      SettingsSwitch(
        state = trackBackgroundState,
        title = { Text(BACKGROUND_ROUTE) },
      ) {
        trackBackgroundState = it
        kvTable.putAny(BACKGROUND_ROUTE, it)
      }
      SettingsSlider(
        title = { Text(SAMPLE_RATE_ROUTE) },
        subtitle = { Text("${Base.roundToTwoDecimals(sampleRateState)} s") },
        value = sampleRateState,
        valueRange = 0.5f..20.0f,
        onValueChange = { sampleRateState = it },
        onValueChangeFinished = {
          kvTable.putAny(SAMPLE_RATE_ROUTE, sampleRateState)
        }
      )
    }
  }

  @Composable
  fun OtherSettings() {
    SettingsGroup(
      modifier = Modifier,
      title = { Text("Other") },
      contentPadding = groupPaddingValues,
      enabled = true
    ) {

      SettingsMenuLink(title = { Text("Export data") }) { TODO() }
      SettingsMenuLink(title = { Text("Import data") }) { TODO() }
    }
  }
}
