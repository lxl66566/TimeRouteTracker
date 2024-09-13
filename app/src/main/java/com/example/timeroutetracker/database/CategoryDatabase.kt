package com.example.timeroutetracker.database

/*
 * 存储分类信息
 */
data class CategoryInfo(
  val categoryName: String,
  val categoryColor: Int,
)

data class CategoryInfoWithId(
  val id: Int,
  val categoryName: String,
  val categoryColor: Int,
)