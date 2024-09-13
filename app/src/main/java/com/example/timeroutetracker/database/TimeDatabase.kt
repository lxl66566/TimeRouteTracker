package com.example.timeroutetracker.database

import java.time.Duration
import java.time.LocalDateTime


/*
 * 存储应用信息
 */
data class AppInfo(
  val packageName: String,
  val appName: String,
  val categoryId: Int,
)

/*
 * 每一条用时记录，适用于 hours, daily, monthly, yearly
 */
data class AppTimeRecord(
  val appId: Int,
  val time: Duration,
  val datatime: LocalDateTime,
)

data class AppTimeInsertion(
  val packageName: String,
  val time: Duration,
  val datetime: LocalDateTime,
)

/*
 * 查询的结果结构体
 */
data class AppTimeQuery(
  // 应用名
  val appName: String,
  // 分类的颜色
  val categoryColor: Int,
  // 开始时间
  val datetime: LocalDateTime,
  // 持续时间
  val time: Duration,
)

typealias AppTimeQueries = List<AppTimeQuery>


