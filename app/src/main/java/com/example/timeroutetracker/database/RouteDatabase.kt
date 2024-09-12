package com.example.timeroutetracker.database

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class RouteItem(
  val time: LocalDateTime,
  val location: LatLng,
)

typealias RouteInfo = MutableList<LatLng>