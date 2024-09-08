package com.example.timeroutetracker.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MySerde {
  companion object {
    public inline fun <reified T> serialize(ob: T): String {
      return Json.encodeToString(ob)
    }

    public inline fun <reified T> deserialize(ob: Any): T {
      return Json.decodeFromString(ob.toString())
    }
  }
}