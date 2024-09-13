package com.example.timeroutetracker.database

import java.time.Duration
import java.util.Date

/*
 * 存储分类信息
 */
data class CategoryInfo(
  val categoryName: String,
  val categoryColor: Int,
)

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
  val date: Date,
)

/*
 * 查询的结果结构体
 */
data class AppTimeQuery(
  // 应用名
  val appName: String,
  // 分类
  val categoryId: Int,
  // 开始时间
  val date: Date,
  // 持续时间
  val time: Duration,
)

typealias AppTimeQueries = List<AppTimeQuery>
