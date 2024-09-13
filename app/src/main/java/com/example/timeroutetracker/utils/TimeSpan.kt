package com.example.timeroutetracker.utils

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

enum class SpanType {
  HOUR,
  DAY,
  WEEK,
  MONTH,
  YEAR
}

interface splitableTimeSpan {
  fun splitTimeSpan(): List<TimeSpan>
}

open class TimeSpan(
  public val start: LocalDateTime,
  public val end: LocalDateTime
) {
  init {
    require(!end.isBefore(start)) { "End time must be after start time." }
  }

  companion object {
    /*
     * Get the timespan of the current day
     */
    fun today(): TimeSpan {
      val todayStart = LocalDate.now().atStartOfDay()
      val todayEnd = todayStart.plusDays(1).minusNanos(1)
      return TimeSpan(todayStart, todayEnd)
    }

    // Create a TimeSpan from a specific point in time to now
    fun from(start: LocalDateTime): TimeSpan {
      return TimeSpan(start, LocalDateTime.now())
    }

    fun fromNow(duration: Duration): TimeSpan {
      val now = LocalDateTime.now()
      return TimeSpan(now, now.plus(duration))
    }
  }

  // Convert TimeSpan to Duration
  fun toDuration(): Duration = Duration.between(start, end)

  // Get start and end time in Millis since the epoch
  fun startInMillis(): Long = start.toInstant(ZoneOffset.UTC).toEpochMilli()
  fun endInMillis(): Long = end.toInstant(ZoneOffset.UTC).toEpochMilli()


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


  open fun getSpanType(): SpanType {
    return when {
      isHour() -> SpanType.HOUR
      isDay() -> SpanType.DAY
      isMonth() -> SpanType.MONTH
      isYear() -> SpanType.YEAR
      else -> throw IllegalStateException("The current span type is not recognized.")
    }
  }

  fun isHour(): Boolean {
    return (Duration.between(start, end).abs() - Duration.ofHours(1)).abs() < Duration.ofMinutes(1)
  }

  fun isDay(): Boolean {
    return (Duration.between(start, end).abs() - Duration.ofDays(1)).abs() < Duration.ofMinutes(1)
  }

  fun isWeek(): Boolean {
    return (Duration.between(start, end).abs() - Duration.ofDays(7)).abs() < Duration.ofMinutes(10)
  }

  fun isMonth(): Boolean {
    return (Duration.between(start, end).abs() - Duration.ofDays(30)).abs() < Duration.ofDays(5)
  }

  fun isYear(): Boolean {
    return (Duration.between(start, end).abs() - Duration.ofDays(365)).abs() < Duration.ofDays(5)
  }


  open fun prevSpan(): TimeSpan {
    return when {
      isHour() -> TimeSpan(start.minusHours(1), end.minusHours(1))
      isDay() -> TimeSpan(start.minusDays(1), end.minusDays(1))
      isWeek() -> TimeSpan(start.minusWeeks(1), end.minusWeeks(1))
      isMonth() -> TimeSpan(start.minusMonths(1), end.minusMonths(1))
      isYear() -> TimeSpan(start.minusYears(1), end.minusYears(1))
      else -> throw IllegalStateException("The current span type is not recognized.")
    }
  }
}

class HourTimeSpan(start: LocalDateTime, end: LocalDateTime) : TimeSpan(start, end) {
  override fun prevSpan(): TimeSpan {
    return TimeSpan(start.minusHours(1), end.minusHours(1))
  }

  override fun getSpanType(): SpanType {
    return SpanType.HOUR
  }
}

class DayTimeSpan(start: LocalDateTime, end: LocalDateTime) : TimeSpan(start, end),
  splitableTimeSpan {

  companion object {
    fun default(): DayTimeSpan {
      val temp = today()
      return DayTimeSpan(temp.start, temp.end)
    }

    fun fromLocalDate(date: LocalDate): DayTimeSpan {
      return DayTimeSpan(date.atStartOfDay(), date.atStartOfDay().plusDays(1).minusNanos(1))
    }
  }

  override fun prevSpan(): TimeSpan {
    return TimeSpan(start.minusDays(1), end.minusDays(1))
  }

  override fun getSpanType(): SpanType {
    return SpanType.DAY
  }

  override fun splitTimeSpan(): List<TimeSpan> {
    val result = mutableListOf<TimeSpan>()
    var currentStart = start

    while (currentStart.isBefore(end)) {
      val currentEnd = currentStart.plusHours(1).coerceAtMost(end)
      result.add(TimeSpan(currentStart, currentEnd))
      currentStart = currentEnd
    }

    return result
  }

}

class WeekTimeSpan(start: LocalDateTime, end: LocalDateTime) : TimeSpan(start, end) {
  override fun prevSpan(): TimeSpan {
    return TimeSpan(start.minusWeeks(1), end.minusWeeks(1))
  }

  override fun getSpanType(): SpanType {
    return SpanType.WEEK
  }
}

class MonthTimeSpan(start: LocalDateTime, end: LocalDateTime) : TimeSpan(start, end) {
  override fun prevSpan(): TimeSpan {
    return TimeSpan(start.minusMonths(1), end.minusMonths(1))
  }

  override fun getSpanType(): SpanType {
    return SpanType.MONTH
  }
}

class YearTimeSpan(start: LocalDateTime, end: LocalDateTime) : TimeSpan(start, end) {
  override fun prevSpan(): TimeSpan {
    return TimeSpan(start.minusYears(1), end.minusYears(1))
  }

  override fun getSpanType(): SpanType {
    return SpanType.YEAR
  }
}