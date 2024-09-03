package com.example.timeroutetracker.utils

import org.junit.Test
import org.junit.Assert.*
import java.time.Duration
import java.time.LocalDateTime

class TimeSpanTest {

  @Test
  fun testToDuration() {
    val start = LocalDateTime.of(2024, 9, 1, 10, 0)
    val end = LocalDateTime.of(2024, 9, 1, 12, 0)
    val timeSpan = TimeSpan(start, end)
    val duration = timeSpan.toDuration()
    assertEquals(Duration.ofHours(2), duration)
  }

  @Test
  fun testStartAndEndInMillis() {
    val start = LocalDateTime.of(2024, 9, 1, 10, 0)
    val end = LocalDateTime.of(2024, 9, 1, 12, 0)
    val timeSpan = TimeSpan(start, end)
    assertTrue(timeSpan.startInMillis() < timeSpan.endInMillis())
  }

  @Test
  fun testFrom() {
    val start = LocalDateTime.now().minusHours(1)
    val timeSpan = TimeSpan.from(start)
    assertEquals(start, timeSpan.start)
    assertTrue(timeSpan.end.isAfter(start))
  }

  @Test
  fun testFromNow() {
    val duration = Duration.ofHours(1)
    val timeSpan = TimeSpan.fromNow(duration)
    assertTrue(timeSpan.end.isAfter(timeSpan.start))
    assertEquals(duration.toMinutes(), timeSpan.toDuration().toMinutes())
  }

  @Test
  fun testContains() {
    val start = LocalDateTime.of(2024, 9, 1, 10, 0)
    val end = LocalDateTime.of(2024, 9, 1, 12, 0)
    val timeSpan = TimeSpan(start, end)
    val timeInRange = LocalDateTime.of(2024, 9, 1, 11, 0)
    val timeOutOfRange = LocalDateTime.of(2024, 9, 1, 13, 0)
    assertTrue(timeSpan.contains(timeInRange))
    assertFalse(timeSpan.contains(timeOutOfRange))
  }

  @Test
  fun testOverlaps() {
    val timeSpan1 = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 10, 0),
      LocalDateTime.of(2024, 9, 1, 12, 0)
    )
    val timeSpan2 = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 11, 0),
      LocalDateTime.of(2024, 9, 1, 13, 0)
    )
    assertTrue(timeSpan1.overlaps(timeSpan2))
    val timeSpan3 = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 13, 0),
      LocalDateTime.of(2024, 9, 1, 14, 0)
    )
    assertFalse(timeSpan1.overlaps(timeSpan3))
  }

  @Test
  fun testIntersection() {
    val timeSpan1 = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 10, 0),
      LocalDateTime.of(2024, 9, 1, 12, 0)
    )
    val timeSpan2 = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 11, 0),
      LocalDateTime.of(2024, 9, 1, 13, 0)
    )
    val intersection = timeSpan1.intersection(timeSpan2)
    assertNotNull(intersection)
    assertEquals(LocalDateTime.of(2024, 9, 1, 11, 0), intersection!!.start)
    assertEquals(LocalDateTime.of(2024, 9, 1, 12, 0), intersection.end)
  }

  @Test
  fun testExtend() {
    val timeSpan = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 10, 0),
      LocalDateTime.of(2024, 9, 1, 12, 0)
    )
    val extended = timeSpan.extend(Duration.ofHours(1))
    assertEquals(LocalDateTime.of(2024, 9, 1, 13, 0), extended.end)
  }

  @Test
  fun testToMinutes() {
    val timeSpan = TimeSpan(
      LocalDateTime.of(2024, 9, 1, 10, 0),
      LocalDateTime.of(2024, 9, 1, 12, 0)
    )
    assertEquals(120, timeSpan.toMinutes())
  }
}
