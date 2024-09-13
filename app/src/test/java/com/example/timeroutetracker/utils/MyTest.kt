package com.example.timeroutetracker.utils

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class MyTest {

  @Test
  fun testPrevSpan() {
    val timeSpan = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 10, 0),
      LocalDateTime.of(2024, 9, 2, 10, 0).minusNanos(1)
    )
    assert(timeSpan.isDay())
    val prevSpan = timeSpan.prevSpan()
    assert(prevSpan.isDay())
    assertEquals(LocalDateTime.of(2024, 8, 31, 10, 0), prevSpan.start)
    assertEquals(LocalDateTime.of(2024, 9, 1, 10, 0).minusNanos(1), prevSpan.end)
  }
}