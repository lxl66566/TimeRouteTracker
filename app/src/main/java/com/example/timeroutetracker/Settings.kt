package com.example.timeroutetracker

import android.content.Context
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.example.timeroutetracker.database.DB
import com.example.timeroutetracker.utils.MySerde


val SETTINGS_TABLE_NAME = "settings"

data class SettingItem(
  val key: String,
  val value: Any // Could be Boolean, String, Enum, or Composable function
)

interface SettingsTrait {
  fun getSetting(key: String): SettingItem?
  fun getSettingOrDefault(key: String, defaultValue: Any): SettingItem?
  fun updateSetting(key: String, value: Any)
}


class Settings(context: Context) {
  val db = DB(context)
  val kvTable = db.kvTable(SETTINGS_TABLE_NAME)

  inline fun <reified T> set(key: String, value: T) {
    kvTable.put(key, MySerde.serialize(value))
  }

  inline fun <reified T> get(key: String): T? {
    return kvTable.get(key)?.let { MySerde.deserialize(it) }
  }

  inline fun <reified T> getOrDefault(key: String, defaultValue: T): T {
    return get(key) ?: defaultValue
  }
}

val TRACK_BACKGROUND = "Track route in background"


@Preview(showBackground = true, name = "Settings")
@Composable
fun SettingsView() {
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
    }
  }
}

@Composable
fun TimeSettings() {
  SettingsGroup(
    modifier = Modifier,
    title = { Text("Time Tracker") },
    contentPadding = PaddingValues(8.dp),
    enabled = true
  ) {
    SettingsMenuLink(title = { Text("Change category") }) { }

  }
}

@Composable
fun RouteSettings() {
  SettingsGroup(
    modifier = Modifier,
    title = { Text("Route") },
    contentPadding = PaddingValues(8.dp),
    enabled = true
  ) {
    var trackBackgroundState by remember { mutableStateOf(true) }
    SettingsSwitch(
      state = trackBackgroundState,
      title = { Text(TRACK_BACKGROUND) },
    ) {
      trackBackgroundState = it
    }
  }
}