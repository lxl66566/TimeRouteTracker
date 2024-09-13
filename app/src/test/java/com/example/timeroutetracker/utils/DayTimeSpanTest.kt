package com.example.timeroutetracker.utils

import org.junit.Test
import kotlin.test.assertEquals


class DayTimeSpanTest {

  @Test
  fun splitTimeSpan() {
    val span = DayTimeSpan.default()
    val split = span.splitTimeSpan()
    assertEquals(split.size, 24)
    split.forEach {
      assertEquals(it.getSpanType(), SpanType.HOUR)
    }
  }
}