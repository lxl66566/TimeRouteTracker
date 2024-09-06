package com.example.timeroutetracker.components

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Duration
import java.util.Date

@Database(
  entities = [AppInfo::class, AppTimeRecord::class, AppTimeQuery::class, CategoryInfo::class],
  version = 1
)
@TypeConverters(DurationConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appTimeQueryDao(): AppTimeDao
}

@Dao
interface AppTimeDao {
  @Query(
    """
        SELECT ai.app_name AS appName, 
               ai.category AS categoryId, 
               atr.date AS date, 
               atr.time AS time 
        FROM AppInfo ai 
        JOIN AppTimeRecord atr ON ai.id = atr.app_id
        """
  )
  fun getAllAppTimeQueries(): List<AppTimeQuery>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAppInfo(appInfo: AppInfo)

  @Query("UPDATE AppInfo SET category = :newCategoryId WHERE package_name = :packageName")
  fun updateAppCategoryByPackageName(packageName: String, newCategoryId: Int)

  @Query(
    """
    UPDATE AppTimeRecord 
    SET time = :newTime 
    WHERE date = :date AND app_id = (
        SELECT id FROM AppInfo WHERE app_name = :appName
    )
    """
  )
  fun updateAppTimeByDateAndName(date: Date, appName: String, newTime: Duration)

  @Query("SELECT * FROM CategoryInfo WHERE id = :id")
  fun getCategoryInfoById(id: Int): CategoryInfo?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertCategoryInfo(categoryInfo: CategoryInfo)

  @Insert
  fun insertAppTimeRecord(appTimeRecord: AppTimeRecord)
}

@Entity
data class CategoryInfo(
  @PrimaryKey
  val id: Int,
  @ColumnInfo(name = "category_name")
  val categoryName: String,
  @ColumnInfo(name = "category_color")
  val categoryColor: Int,
)

/*
 * 存储应用信息
 */
@Entity
data class AppInfo(
  @PrimaryKey
  val id: Int,
  @ColumnInfo(name = "package_name")
  val packageName: String,
  @ColumnInfo(name = "app_name")
  val appName: String,
  @ColumnInfo(name = "category")
  val categoryId: Int,
)

/*
 * 每一条用时记录，适用于 hours, daily, monthly, yearly
 */
@Entity
data class AppTimeRecord(
  @PrimaryKey
  val id: Long,
  @ColumnInfo(name = "app_id")
  val appId: Int,
  @ColumnInfo(name = "time")
  val time: Duration,
  @ColumnInfo(name = "date")
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

class DurationConverters {
  @TypeConverter
  fun fromDuration(duration: Duration?): Long? {
    return duration?.seconds
  }

  @TypeConverter
  fun toDuration(seconds: Long?): Duration? {
    return seconds?.let { Duration.ofSeconds(it) }
  }
}
