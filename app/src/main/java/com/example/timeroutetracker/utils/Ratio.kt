package com.example.timeroutetracker.utils


import android.util.Log
import androidx.compose.ui.unit.Dp

val RatioTag = "Ratio"

public fun linearRatio(aMin: Dp, aMax: Dp, bMin: Dp, bExpectedMax: Dp, x: Dp): Pair<Dp, Dp> {
  require(aMin < aMax && bMin < bExpectedMax) { "Min value should be less than max" }

  if (x >= (aMax + bExpectedMax)) return Pair(aMax, bExpectedMax)
  if (x < (aMin + bMin)) {
    Log.w(RatioTag, "linear: x < (aMin + bMin)")
    return Pair(aMin, bMin)
  }

  val aRatio =
    (aMax.value - aMin.value) / (aMax.value - aMin.value + bExpectedMax.value - bMin.value)
  val remain = x.value - (aMin.value + bMin.value)

  val aResult = Dp(aRatio * remain + aMin.value)
  val bResult = Dp(remain * (1 - aRatio) + bMin.value)

  return Pair(aResult, bResult)
}

