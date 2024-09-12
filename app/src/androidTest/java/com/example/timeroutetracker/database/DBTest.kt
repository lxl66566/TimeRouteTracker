package com.example.timeroutetracker.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class DBTest {

  private lateinit var context: Context
  private lateinit var db: DB
  private lateinit var kvTable: DB.KVTable

  enum class TestEnum {
    VALUE1, VALUE2
  }

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = DB(context, "test_database")
    kvTable = db.kvTable("test_kv_table")
  }

  @Test
  fun testKVGetOrInit() {
    // Ensure the table is empty
    kvTable.delete("test_key")

    // Use getOrInit and check default value insertion
    val defaultValue = "default_value"
    val value = kvTable.getOrInit("test_key", defaultValue)
    assertEquals(defaultValue, value, "Value should be the default value")

    // Retrieve the value again to ensure it was correctly inserted
    val retrievedValue = kvTable.get("test_key")
    assertEquals(defaultValue, retrievedValue, "Retrieved value should match the default value")

    // Update the value and check if the new value is retrieved
    val newValue = "new_value"
    kvTable.put("test_key", newValue)
    val updatedValue = kvTable.getOrInit("test_key", defaultValue)
    assertEquals(newValue, updatedValue, "Updated value should be returned, not the default value")
  }

  @Test
  fun testKVPutGetAny() {
    // Ensure the table is empty
    kvTable.delete("test_key")

    // Use getOrInitAny and check default value insertion
    val defaultValue = 1.35f
    val value = kvTable.getOrInitAny("test_key", defaultValue)
    assertEquals(defaultValue, value, "Value should be the default value")

    // Retrieve the value again to ensure it was correctly inserted
    val retrievedValue = kvTable.getAny<Float>("test_key")
    assertEquals(
      defaultValue,
      retrievedValue,
      "Retrieved value should be same to the default value"
    )

    // Test enum
    val newValue = TestEnum.VALUE1
    kvTable.putAny("test_key", newValue)
    val updatedValue = kvTable.getAny<TestEnum>("test_key")
    assertEquals(newValue, updatedValue, "Enum value should be returned")
  }

//  @Test(expected = InvalidSqliteException::class)
//  fun testImportFromInvalidFile() {
//    val contentValues = ContentValues().apply {
//      put(MediaStore.Downloads.DISPLAY_NAME, "test_file.txt")
//      put(MediaStore.Downloads.MIME_TYPE, "text/plain")
//      put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // 保存到 Downloads 目录
//    }
//
//    val uri: Uri? =
//      context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
//
//    uri?.let {
//      context.contentResolver.openOutputStream(it)?.use { outputStream ->
//        outputStream.write("asd123".toByteArray())
//      }
//      db.importDatabaseFromUri(it)
//      Log.i("FileWrite", "Imported successfully")
//    } ?: run {
//      // 处理插入失败的情况
//      Log.e("FileWrite", "Failed to create file in Downloads")
//    }
//  }

  @Test
  fun testRouteTable() {
    val routeTable: DB.routeTable = db.routeTable("test_route_table1")
    val items = listOf(
      RouteItem(LocalDateTime.of(2024, 1, 1, 12, 0), LatLng(37.7749, -122.4194)),
      RouteItem(LocalDateTime.of(2024, 1, 1, 13, 0), LatLng(37.7833, -122.4167)),
      RouteItem(LocalDateTime.of(2024, 1, 1, 14, 0), LatLng(37.7852, -122.4151)),
      RouteItem(LocalDateTime.of(2024, 1, 1, 15, 0), LatLng(37.7854, -122.4101)),
    )
    items.forEach { routeTable.insertRouteItem(it) }
    val span =
      routeTable.getSpan(LocalDateTime.of(2024, 1, 1, 12, 0), LocalDateTime.of(2024, 1, 1, 14, 0))
    assertEquals(3, span.size, "Span should be 3")
    assertEquals(
      span,
      listOf(items[0], items[1], items[2]).map { it.location },
      "Items should be the same as span()"
    )
  }
}
