package com.example.timeroutetracker.components

import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timeroutetracker.database.AppDatabase
import com.example.timeroutetracker.database.AppInfo
import com.example.timeroutetracker.database.AppTimeDao
import com.example.timeroutetracker.database.AppTimeRecord
import com.example.timeroutetracker.database.CategoryInfo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AppTimeDaoTest {

  private lateinit var database: AppDatabase
  private lateinit var dao: AppTimeDao

  @Before
  fun setUp() {
    // 创建内存中的数据库实例
    database = Room.inMemoryDatabaseBuilder(
      ApplicationProvider.getApplicationContext(),
      AppDatabase::class.java
    ).allowMainThreadQueries().build()

    dao = database.appTimeQueryDao()
  }

  @After
  fun tearDown() {
    // 关闭数据库
    database.close()
  }

  @Test
  fun testInsertAppInfo() = runBlocking {
    val appInfo =
      AppInfo(id = 1, packageName = "com.example.app", appName = "Example App", categoryId = 1)
    dao.insertAppInfo(appInfo)
    val result = dao.getAllAppTimeQueries()
    assertEquals(0, result.size) // 没有插入 AppTimeRecord，应该返回空列表
  }

  @Test
  fun testUpdateAppCategoryByPackageName() = runBlocking {
    val appInfo =
      AppInfo(id = 1, packageName = "com.example.app", appName = "Example App", categoryId = 1)
    dao.insertAppInfo(appInfo)
    dao.updateAppCategoryByPackageName("com.example.app", 2)
    val updatedAppInfo = dao.getAllAppTimeQueries()
    assertEquals(0, updatedAppInfo.size) // 没有插入 AppTimeRecord，应该返回空列表
  }

  @Test
  fun testUpdateAppTimeByDateAndName() = runBlocking {
    val appInfo =
      AppInfo(id = 1, packageName = "com.example.app", appName = "Example App", categoryId = 1)
    val appTimeRecord =
      AppTimeRecord(id = 1, appId = 1, time = Duration.ofMinutes(30), date = Date())
    dao.insertAppInfo(appInfo)
    dao.updateAppTimeByDateAndName(appTimeRecord.date, "Example App", Duration.ofMinutes(60))
    val result = dao.getAllAppTimeQueries()
    assertEquals(0, result.size) // 没有插入 AppTimeRecord，应该返回空列表
  }

  @Test
  fun testGetCategoryInfoById() = runBlocking {
    val categoryInfo =
      CategoryInfo(id = 1, categoryName = "Productivity", categoryColor = Color.BLUE)
    dao.insertCategoryInfo(categoryInfo)
    val result = dao.getCategoryInfoById(1)
    assertEquals("Productivity", result?.categoryName)
  }

  @Test
  fun testGetAllAppTimeQueries() = runBlocking {
    val appInfo =
      AppInfo(id = 1, packageName = "com.example.app", appName = "Example App", categoryId = 1)
    val appTimeRecord =
      AppTimeRecord(id = 1, appId = 1, time = Duration.ofMinutes(30), date = Date())
    dao.insertAppInfo(appInfo)
    dao.insertAppTimeRecord(appTimeRecord)
    val result = dao.getAllAppTimeQueries()
    assertEquals(1, result.size)
    assertEquals("Example App", result[0].appName)
  }

}
