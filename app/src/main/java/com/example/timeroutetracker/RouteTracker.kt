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
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.timeroutetracker.components.EnumDropdownMenu
import com.example.timeroutetracker.components.LocationManager
import com.example.timeroutetracker.database.DB
import com.example.timeroutetracker.database.RouteInfo
import com.example.timeroutetracker.database.RouteItem
import com.example.timeroutetracker.utils.TimeSpan
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.random.Random

private const val TAG = "RouteTracker"

val defaultLatLng = LatLng(1.3588227, 103.8742114)

val defaultCameraPosition = CameraPosition.fromLatLngZoom(defaultLatLng, 11f)
val defaultZoom = 14f

val styleSpan = StyleSpan(
  StrokeStyle.gradientBuilder(
    Color.Red.toArgb(),
    Color.Green.toArgb(),
  ).build(),
)

// 定义一个 ViewModel 来管理后台任务
class RouteTracker(
  db: DB? = null,
  settings: Settings? = null,
  val mock: Boolean = true,  // TODO: remove this
) :
  Fragment() {
  lateinit var db: DB
  lateinit var settings: Settings
  lateinit var locationManager: LocationManager
  lateinit var routeTable: DB.routeTable
  var callback: ((Location) -> Unit)? = null
  private val viewModelJob = Job()
  private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    db = DB(context)
    settings = Settings(db)
    if (!mock)
      routeTable = db.routeTable()
    else
      routeTable = db.routeTable("RouteMock")
  }

  override fun onDetach() {
    super.onDetach()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // 启动后台任务
    val intervalSetting = settings.getProxySettings().getSampleRate()
    locationManager = LocationManager(this, (intervalSetting * 1000).toLong())
    callback.let { locationManager.setCallback(it) }
  }

  override fun onDestroy() {
    super.onDestroy()
//    locationManager.stopLocationUpdates()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        MaterialTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            RouteTrackerView()
          }
        }
      }
    }
  }


  /*
   * The data could not be null!!!
   */
  private val _data: MutableLiveData<RouteInfo> =
    MutableLiveData<RouteInfo>(mutableListOf(defaultLatLng))
  val data get() = _data

  @Composable
  fun RouteTrackerView() {
    var isMapLoaded by remember { mutableStateOf(false) }

    Box(
      modifier = Modifier
        .fillMaxSize()
    ) {
      GoogleMapView(
        onMapLoaded = {
          isMapLoaded = true
        },
      )
//      if (!isMapLoaded) {
//        AnimatedVisibility(
//          modifier = Modifier
//            .matchParentSize(),
//          visible = !isMapLoaded,
//          enter = EnterTransition.None,
//          exit = fadeOut()
//        ) {
//          CircularProgressIndicator(
//            modifier = Modifier
//              .background(MaterialTheme.colorScheme.background)
//              .wrapContentSize()
//          )
//        }
//      }
    }
  }

  @Composable
  fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
      position = defaultCameraPosition
    },
    onMapLoaded: () -> Unit = {},
//  mapColorScheme: ComposeMapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
    content: @Composable () -> Unit = {}
  ) {
    // constant values
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
    val mapVisible by remember { mutableStateOf(true) }
    val startPosState = rememberMarkerState(position = defaultLatLng)
    val polylineSpanPointsMocked: MutableState<RouteInfo> =
      remember { mutableStateOf(mutableListOf(defaultLatLng)) }

    // get database
    val todayData = routeTable.getSpan(TimeSpan.today())
    if (todayData.isNotEmpty()) {
      if (!mock) {
        _data.value?.clear()
        _data.value?.addAll(todayData)
      } else {
        polylineSpanPointsMocked.value = todayData
      }
      todayData.first().let {
        startPosState.position = it
        cameraPositionState.position = CameraPosition.fromLatLngZoom(it, defaultZoom)
      }
    }

    val polylineSpanPoints: RouteInfo? = data.observeAsState().value
    if (!mock) {
      callback = { location ->
        // Handle location update
        val latlng = LatLng(location.latitude, location.longitude)
        Log.i(TAG, "get Location: $latlng")
        if (_data.value?.size == 1 && _data.value?.first()!! == defaultLatLng) {
          _data.value?.set(0, latlng)
          startPosState.position = latlng
          cameraPositionState.position = CameraPosition.fromLatLngZoom(latlng, defaultZoom)
        } else {
          _data.value?.add(latlng)
        }
        routeTable.insertRouteItem(RouteItem(LocalDateTime.now(), latlng))
      }
      locationManager.setCallback(callback)
//    locationManager.startLocationUpdates()

    } else {
      // mock data
      val intervalSetting = settings.getProxySettings().getSampleRate()
      coroutineScope.launch {
        Log.d(TAG, "launched a coroutine to update data")
        while (true) {
          val last = polylineSpanPointsMocked.value.last()
          val new = generateRandomLatLng(last)
          withContext(Dispatchers.Main) {
            polylineSpanPointsMocked.value.add(new)
          }
          routeTable.insertRouteItem(RouteItem(LocalDateTime.now(), new))
          delay((intervalSetting * 1000).toLong())
        }
      }
    }


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
          state = startPosState,
          title = "Start",
          draggable = false,
        ) {
          Text(it.title ?: "Title", color = Color.Red)
        }
        if (!mock) {
          Polyline(
            points = polylineSpanPoints ?: emptyList(),
            spans = styleSpanList,
            tag = "Polyline",
          )
        } else {
          Polyline(
            points = polylineSpanPointsMocked.value,
            spans = styleSpanList,
            tag = "Polyline",
          )
        }


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
            startPosState.position = defaultLatLng
            startPosState.hideInfoWindow()
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
        MapButton(text = "Test", onClick = {
          val test = locationManager.getLocation()
          Log.i(TAG, "test: $test")
          test?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(test, defaultZoom)
          }
        })
      }
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

fun generateRandomLatLng(center: LatLng, delta: Double = 0.001): LatLng {
  Log.d(TAG, "generateRandomLatLng: center: $center")
  val random = Random
  // 生成一个介于 -delta 和 delta 之间的随机纬度偏移量
  val latOffset = (random.nextDouble() * 2 * delta) - delta
  // 生成一个介于 -delta 和 delta 之间的随机经度偏移量
  val lngOffset = (random.nextDouble() * 2 * delta) - delta

  // 应用偏移量得到新的经纬度
  return LatLng(
    center.latitude + latOffset,
    center.longitude + lngOffset
  ).also {
    // 确保纬度在有效范围内 (-90 to 90)
    if (it.latitude < -90 || it.latitude > 90) throw IllegalArgumentException("Latitude out of bounds")
    // 确保经度在有效范围内 (-180 to 180)
    if (it.longitude < -180 || it.longitude > 180) throw IllegalArgumentException("Longitude out of bounds")
  }
}