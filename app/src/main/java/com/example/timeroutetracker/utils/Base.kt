package com.example.timeroutetracker.utils

import java.math.RoundingMode
import java.text.DecimalFormat

class Base {
  companion object {
    fun roundToTwoDecimals(number: Float): Float {
      val decimalFormat = DecimalFormat("0.00")
      decimalFormat.roundingMode = RoundingMode.HALF_UP
      return decimalFormat.format(number).toFloat()
    }
  }
}