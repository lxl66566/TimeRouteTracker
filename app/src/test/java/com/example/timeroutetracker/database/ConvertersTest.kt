package com.example.timeroutetracker.database

import com.google.android.gms.maps.model.LatLng
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals


class ConvertersTest {
  @Test
  fun `test DurationConverters`() {
    val duration = Duration.ofSeconds(123L)
    val converters = DurationConverter()

    assertEquals(123L, converters.from(duration))
    assertEquals(duration, converters.to(123L))
  }

  @Test
  fun `test LocalDateTimeConverters`() {
    val localDateTime = LocalDateTime.of(2024, 9, 12, 10, 9)
    val converters = LocalDateTimeConverter()

    val epochSeconds = localDateTime.toEpochSecond(ZoneOffset.UTC)
    assertEquals(epochSeconds, converters.from(localDateTime))
    assertEquals(localDateTime, converters.to(epochSeconds))
  }

  @Test
  fun `test LatLngConverter`() {
    val latLng = LatLng(1.0, 2.0)
    val converters = LatLngConverter()

    val bytes = converters.from(latLng)
    assertEquals(latLng, converters.to(bytes))
  }
}