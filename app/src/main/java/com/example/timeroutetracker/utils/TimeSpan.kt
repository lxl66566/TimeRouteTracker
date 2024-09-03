package com.example.timeroutetracker.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class TimeSpan(
  public val start: LocalDateTime,
  public val end: LocalDateTime
) {
  init {
    require(!end.isBefore(start)) { "End time must be after start time." }
  }

  // Convert TimeSpan to Duration
  fun toDuration(): Duration = Duration.between(start, end)

  // Get start and end time in Millis since the epoch
  fun startInMillis(): Long = start.toInstant(ZoneOffset.UTC).toEpochMilli()
  fun endInMillis(): Long = end.toInstant(ZoneOffset.UTC).toEpochMilli()

  // Create a TimeSpan from a specific point in time to now
  companion object {
    fun from(start: LocalDateTime): TimeSpan {
      return TimeSpan(start, LocalDateTime.now())
    }

    fun fromNow(duration: Duration): TimeSpan {
      val now = LocalDateTime.now()
      return TimeSpan(now, now.plus(duration))
    }
  }

  // Check if a certain time point is within the timespan
  fun contains(time: LocalDateTime): Boolean {
    return !time.isBefore(start) && !time.isAfter(end)
  }

  // Check if two TimeSpans overlap
  fun overlaps(other: TimeSpan): Boolean {
    return start.isBefore(other.end) && end.isAfter(other.start)
  }

  // Calculate the intersection of two TimeSpans
  fun intersection(other: TimeSpan): TimeSpan? {
    val newStart = maxOf(start, other.start)
    val newEnd = minOf(end, other.end)
    return if (!newEnd.isBefore(newStart)) TimeSpan(newStart, newEnd) else null
  }

  // Extend the TimeSpan by a certain duration
  fun extend(duration: Duration): TimeSpan {
    return TimeSpan(start, end.plus(duration))
  }

  // Get the total minutes of the TimeSpan
  fun toMinutes(): Long {
    return ChronoUnit.MINUTES.between(start, end)
  }

  override fun toString(): String {
    return "TimeSpan(start=$start, end=$end)"
  }
}
