package com.example.timeroutetracker.components

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AppTimeQueryDaoTest {

  private lateinit var database: AppDatabase
  private lateinit var dao: AppTimeQueryDao

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
  fun testGetAllAppTimeQueries() {
    // 插入测试数据
    val appInfo1 = AppInfo(
      id = 1,
      packageName = "com.example.app1",
      appName = "Example App 1",
      categoryId = 101
    )
    val appInfo2 = AppInfo(
      id = 2,
      packageName = "com.example.app2",
      appName = "Example App 2",
      categoryId = 102
    )

    val timeRecord1 = AppTimeRecord(
      id = 1L,
      appId = 1,
      time = Duration.ofMinutes(30),
      date = Date()
    )
    val timeRecord2 = AppTimeRecord(
      id = 2L,
      appId = 2,
      time = Duration.ofMinutes(45),
      date = Date()
    )

    // 插入数据到数据库
    database.runInTransaction {
      database.appTimeQueryDao().apply {
        database.appTimeQueryDao().apply {
          // 插入 AppInfo 数据
          database.compileStatement("INSERT INTO AppInfo (id, package_name, app_name, category) VALUES (1, 'com.example.app1', 'Example App 1', 101)")
            .executeInsert()
          database.compileStatement("INSERT INTO AppInfo (id, package_name, app_name, category) VALUES (2, 'com.example.app2', 'Example App 2', 102)")
            .executeInsert()

          // 插入 AppTimeRecord 数据
          database.compileStatement(
            "INSERT INTO AppTimeRecord (id, app_id, time, date) VALUES (1, 1, ${
              Duration.ofMinutes(
                30
              ).seconds
            }, ${Date().time})"
          ).executeInsert()
          database.compileStatement(
            "INSERT INTO AppTimeRecord (id, app_id, time, date) VALUES (2, 2, ${
              Duration.ofMinutes(
                45
              ).seconds
            }, ${Date().time})"
          ).executeInsert()
        }

        // 执行查询
        val results = getAllAppTimeQueries()

        // 验证结果
        assertEquals(2, results.size)

        // 验证第一个结果
        val firstResult = results[0]
        assertEquals("Example App 1", firstResult.appName)
        assertEquals(101, firstResult.categoryId)
        assertEquals(timeRecord1.time, firstResult.time)

        // 验证第二个结果
        val secondResult = results[1]
        assertEquals("Example App 2", secondResult.appName)
        assertEquals(102, secondResult.categoryId)
        assertEquals(timeRecord2.time, secondResult.time)
      }
    }
  }
}
