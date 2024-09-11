// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// https://github.com/googlemaps/android-maps-compose/blob/main/app/src/main/java/com/google/maps/android/compose/BasicMapActivity.kt

package com.example.timeroutetracker

//import com.google.maps.android.compose.theme.MapsComposeSampleTheme
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.timeroutetracker.components.EnumDropdownMenu
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val TAG = "BasicMapActivity"

val singapore = LatLng(1.3588227, 103.8742114)
val singapore5 = LatLng(1.3418, 103.8461)
val singapore6 = LatLng(1.3430, 103.8844)
val singapore7 = LatLng(1.3430, 103.9116)
val singapore8 = LatLng(1.3300, 103.8624)

val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

val styleSpan = StyleSpan(
  StrokeStyle.gradientBuilder(
    Color.Red.toArgb(),
    Color.Green.toArgb(),
  ).build(),
)

class BasicMapActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isMapLoaded by remember { mutableStateOf(false) }
      // Observing and controlling the camera's state can be done with a CameraPositionState
      val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .systemBarsPadding()
      ) {
        GoogleMapView(
          cameraPositionState = cameraPositionState,
          onMapLoaded = {
            isMapLoaded = true
          },
        )
        if (!isMapLoaded) {
          AnimatedVisibility(
            modifier = Modifier
              .matchParentSize(),
            visible = !isMapLoaded,
            enter = EnterTransition.None,
            exit = fadeOut()
          ) {
            CircularProgressIndicator(
              modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .wrapContentSize()
            )
          }
        }
      }
    }
  }
}

@Composable
fun GoogleMapView(
  modifier: Modifier = Modifier,
  cameraPositionState: CameraPositionState = rememberCameraPositionState(),
  onMapLoaded: () -> Unit = {},
//  mapColorScheme: ComposeMapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
  content: @Composable () -> Unit = {}
) {
  cameraPositionState.position = defaultCameraPosition
  val singaporeState = rememberMarkerState(position = singapore)
  val polylineSpanPoints = remember { listOf(singapore, singapore6, singapore7) }
  val styleSpanList = remember { listOf(styleSpan) }
  val uiSettings by remember {
    mutableStateOf(
      MapUiSettings(
        compassEnabled = true,
        zoomControlsEnabled = true,
        myLocationButtonEnabled = true
      )
    )
  }
  var mapType by remember { mutableStateOf(MapType.NORMAL) }
  var mapProperties by remember {
    mutableStateOf(MapProperties(mapType = mapType))
  }
  var mapVisible by remember { mutableStateOf(true) }

//  var darkMode by remember { mutableStateOf(mapColorScheme) }

  if (mapVisible) {
    GoogleMap(
      modifier = modifier,
      cameraPositionState = cameraPositionState,
      properties = mapProperties,
      uiSettings = uiSettings,
      onMapLoaded = onMapLoaded,
      onPOIClick = {
        Log.d(TAG, "POI clicked: ${it.name}")
      },
//      mapColorScheme = darkMode
    ) {
      MarkerInfoWindowContent(
        state = singaporeState,
        title = "Start",
        draggable = false,
      ) {
        Text(it.title ?: "Title", color = Color.Red)
      }

      Polyline(
        points = polylineSpanPoints,
        spans = styleSpanList,
        tag = "Polyline B",
      )

      content()
    }
  }
  Column {
    Row(modifier = Modifier.padding(horizontal = 5.dp)) {
      EnumDropdownMenu(
        textStyle = MaterialTheme.typography.labelSmall,
        contentPadding = PaddingValues(2.dp),
        enumClass = MapType::class.java,
        selectedEnum = mapType,
      ) {
        mapType = it
        mapProperties = mapProperties.copy(mapType = it)
      }
      MapButton(
        text = "Reset Map",
        onClick = {
          mapProperties = mapProperties.copy(mapType = MapType.NORMAL)
          cameraPositionState.position = defaultCameraPosition
          singaporeState.position = singapore
          singaporeState.hideInfoWindow()
        }
      )
//      MapButton(
//        text = "Toggle Dark Mode",
//        onClick = {
//          darkMode =
//            if (darkMode == ComposeMapColorScheme.DARK)
//              ComposeMapColorScheme.LIGHT
//            else
//              ComposeMapColorScheme.DARK
//        },
//        modifier = Modifier
//          .testTag("toggleDarkMode")
//      )
    }
  }
}


@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(
    modifier = modifier.padding(horizontal = 5.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.onPrimary,
      contentColor = MaterialTheme.colorScheme.primary
    ),
    contentPadding = PaddingValues(2.dp),
    onClick = onClick
  ) {
    Text(text = text, style = MaterialTheme.typography.labelSmall)
  }
}

