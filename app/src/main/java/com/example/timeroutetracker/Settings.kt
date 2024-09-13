package com.example.timeroutetracker

import android.content.Context
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlin.system.exitProcess


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

  private var kvTable = db.kvTable(SETTINGS_TABLE_NAME)
  private val groupPaddingValues = PaddingValues(8.dp)
  private val proxy = SettingsProxy()

  fun getProxySettings(): SettingsProxy = proxy

  data class SettingsData(var trackBackground: Boolean, var sampleRate: Float)

  open inner class SettingsProxy() {

    private var settingsData: SettingsData = SettingsData(
      kvTable.get(BACKGROUND_ROUTE)?.toBoolean() ?: BACKGROUND_ROUTE_DEFAULT,
      kvTable.get(SAMPLE_RATE_ROUTE)?.toFloat() ?: SAMPLE_RATE_ROUTE_DEFAULT
    )

    fun getSettingsData() = settingsData
    fun getTrackBackground() = settingsData.trackBackground
    fun getSampleRate() = settingsData.sampleRate

    fun setSettingsData(otherData: SettingsData) {
      setTrackBackground(otherData.trackBackground)
      setSampleRate(otherData.sampleRate)
    }

    fun setTrackBackground(trackBackground: Boolean) {
      kvTable.put(BACKGROUND_ROUTE, trackBackground.toString())
      settingsData.trackBackground = trackBackground
    }

    fun setSampleRate(sampleRate: Float) {
      kvTable.put(SAMPLE_RATE_ROUTE, sampleRate.toString())
      settingsData.sampleRate = sampleRate
    }

    fun clear() {
      this.settingsData = SettingsProxy().getSettingsData()
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
          getProxySettings().getTrackBackground()
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
        getProxySettings().setTrackBackground(it)
      }
      SettingsSlider(
        title = { Text(SAMPLE_RATE_ROUTE) },
        subtitle = { Text("${Base.roundToTwoDecimals(sampleRateState)} s") },
        value = sampleRateState,
        valueRange = 0.5f..20.0f,
        onValueChange = { sampleRateState = it },
        onValueChangeFinished = {
          getProxySettings().setSampleRate(sampleRateState)
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
      var showAlertDialog by remember { mutableStateOf(false) }
      val context = LocalContext.current

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

      SettingsMenuLink(title = { Text("Clear data!!") }) {
        showAlertDialog = true
      }

      if (showAlertDialog) {
        clearDataAlertDialog(context, onSuccess = {
          getProxySettings().clear()
        }) {
          showAlertDialog = false
        }
      }
    }


  }

  @Composable
  fun clearDataAlertDialog(
    context: Context,
    onSuccess: () -> Unit = {},
    closeDialog: () -> Unit = {}
  ) {
    AlertDialog(
      onDismissRequest = {
        // Dismiss the dialog when the user clicks outside the dialog or on the back
        // button. If you want to disable that functionality, simply use an empty
        // onCloseRequest.
        closeDialog()
      },
      title = { Text("Clear data") },
      text = { Text("Are you sure?") },
      confirmButton = {
        TextButton(
          onClick = {
            db.deleteDatabase()
            Toast.makeText(
              context,
              "Cleared successfully",
              Toast.LENGTH_LONG
            ).show()
            onSuccess()
            closeDialog()
            exitProcess(0)
          }
        ) {
          Text("Confirm")
        }
      }
    )
  }
}
