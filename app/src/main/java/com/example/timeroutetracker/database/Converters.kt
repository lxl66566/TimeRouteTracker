@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.example.timeroutetracker.database

import com.google.android.gms.maps.model.LatLng
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

open class Converters<T, R> {
  open fun from(in_: T): R = throw NotImplementedError()
  open fun to(out_: R): T = throw NotImplementedError()
}

class DurationConverter : Converters<Duration, Long>() {
  override fun from(duration: Duration): Long {
    return duration.seconds
  }

  override fun to(seconds: Long): Duration {
    return Duration.ofSeconds(seconds)
  }
}

class LocalDateTimeConverter : Converters<LocalDateTime, Long>() {

  override fun from(localDateTime: LocalDateTime): Long {
    return localDateTime.toEpochSecond(ZoneOffset.UTC)
  }

  override fun to(seconds: Long): LocalDateTime {
    return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC)
  }
}

//class LatLngConverter : Converters<LatLng, String>() {
//
//  override fun from(loc: LatLng): String {
//    return "${loc.latitude},${loc.longitude}"
//  }
//
//  override fun to(str: String): LatLng {
//    val parts = str.split(',')
//    if (parts.size != 2) throw IllegalArgumentException("Invalid LatLng string format.")
//    return LatLng(parts[0].toDouble(), parts[1].toDouble())
//  }
//}

/*
 * 使用 ByteBuffer 来将 LatLng 对象转换为字节数组，并将字节数组转换回 LatLng 对象，比起直接使用 String 转换
 * 更加高效
 */
class LatLngConverter : Converters<LatLng, ByteArray>() {

  override fun from(in_: LatLng): ByteArray {
    // 将 LatLng 对象转换为字节数组
    return ByteBuffer.allocate(Double.SIZE_BYTES * 2)
      .order(ByteOrder.BIG_ENDIAN)
      .putDouble(in_.latitude)
      .putDouble(in_.longitude)
      .array()
  }

  override fun to(out_: ByteArray): LatLng {
    // 从字节数组转换回 LatLng 对象
    val buffer = ByteBuffer.wrap(out_).order(ByteOrder.BIG_ENDIAN)
    return LatLng(buffer.getDouble(), buffer.getDouble())
  }
}