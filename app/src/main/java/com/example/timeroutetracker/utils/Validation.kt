package com.example.timeroutetracker.utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException

class Validation {
  companion object {
    public fun isSqliteFile(file: File): Boolean {
      val sqliteMagicNumber = byteArrayOf(
        0x53,
        0x51,
        0x4C,
        0x69,
        0x74,
        0x65,
        0x20,
        0x66,
        0x6F,
        0x72,
        0x6D,
        0x61,
        0x74,
        0x20,
        0x33,
        0x00
      )

      return try {
        FileInputStream(file).use { inputStream ->
          val header = ByteArray(sqliteMagicNumber.size)
          if (inputStream.read(header) == sqliteMagicNumber.size) {
            header.contentEquals(sqliteMagicNumber)
          } else {
            false
          }
        }
      } catch (e: IOException) {
        e.printStackTrace()
        false
      }
    }
  }

}