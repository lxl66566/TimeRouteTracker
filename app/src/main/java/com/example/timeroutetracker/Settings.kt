package com.example.timeroutetracker

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import com.example.timeroutetracker.database.DB
import com.example.timeroutetracker.utils.Base
import com.example.timeroutetracker.utils.SettingsNotFoundException


class Settings(private val db: DB) {
  companion object {
    const val SETTINGS_TABLE_NAME = "settings"
    const val BACKGROUND_ROUTE = "Track route in background"
    const val BACKGROUND_ROUTE_DEFAULT = true
    const val SAMPLE_RATE_ROUTE = "GPS sample interval"
    const val SAMPLE_RATE_ROUTE_DEFAULT = 1.0f
    val DEFAULT_EXPORT_FOLDER_URI =
      Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
  }

  private val kvTable = db.kvTable(SETTINGS_TABLE_NAME)
  private val groupPaddingValues = PaddingValues(8.dp)

  fun getSetting(settingItem: String): Any {
    return when (settingItem) {
      BACKGROUND_ROUTE -> kvTable.get(BACKGROUND_ROUTE) ?: BACKGROUND_ROUTE_DEFAULT
      SAMPLE_RATE_ROUTE -> kvTable.get(SAMPLE_RATE_ROUTE) ?: SAMPLE_RATE_ROUTE_DEFAULT
      else -> throw SettingsNotFoundException("$settingItem not found")
    }
  }

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
      SettingsMenuLink(title = { Text("Change category") }) {
        //TODO()
      }
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
            defaultValue = BACKGROUND_ROUTE_DEFAULT
          )
        )
      }
      var sampleRateState by remember {
        mutableStateOf(
          kvTable.getOrInitAny(
            SAMPLE_RATE_ROUTE,
            SAMPLE_RATE_ROUTE_DEFAULT
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

      // TODO: Map provider
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

//      var selectedFolder by remember { mutableStateOf<Uri?>(null) }
//      val exportLauncher: ActivityResultLauncher<Uri?> = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocumentTree(),
//      ) { uri ->
//        selectedFolder = uri
//        val exportTarget =
//          (selectedFolder ?: DEFAULT_EXPORT_URI).buildUpon().appendPath(db.getDatabaseName())
//            .build()
//        db.exportDatabaseToUri(exportTarget)
//      }

      val context = LocalContext.current
      var selectedFile by remember { mutableStateOf<Uri?>(null) }
      val importLauncher: ManagedActivityResultLauncher<Array<String>, Uri?> =
        rememberLauncherForActivityResult(
          contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
          selectedFile = uri
          val importTarget =
            (selectedFile ?: DEFAULT_EXPORT_FOLDER_URI).buildUpon().appendPath(db.getDatabaseName())
              .build()
          db.importDatabaseFromUri(importTarget)
        }

      SettingsMenuLink(title = { Text("Export data") }) {
        try {
          val exportTo =
            DEFAULT_EXPORT_FOLDER_URI.buildUpon().appendPath(db.getDatabaseName()).build()
          val res = db.exportDatabaseToUri(exportTo)
          if (!res) {
            Toast.makeText(
              context,
              "Export failed by unknown reason",
              Toast.LENGTH_SHORT
            ).show()
          } else {
            Toast.makeText(
              context,
              "Export to `${exportTo.path}` successfully",
              Toast.LENGTH_SHORT
            ).show()
          }
        } catch (e: Exception) {
          Toast.makeText(
            context,
            "Export failed: ${e.message}",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
      SettingsMenuLink(title = { Text("Import data") }) {
        try {
          importLauncher.launch(arrayOf("*/*"))
        } catch (e: Exception) {
          Toast.makeText(
            context,
            "Import failed: ${e.message}",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }
}
