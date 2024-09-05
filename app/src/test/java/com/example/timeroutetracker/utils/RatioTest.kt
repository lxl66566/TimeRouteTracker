package com.example.timeroutetracker.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Test

class RatioTest {
  private fun approx(a: Dp, b: Dp) = (a - b).value < 0.01

  @Test
  fun testLinear() {
    val result = linearRatio(0.dp, 100.dp, 0.dp, 200.dp, 51.dp)
    assert(approx(result.first, 17.dp))
    assert(approx(result.second, 34.dp))
  }
}