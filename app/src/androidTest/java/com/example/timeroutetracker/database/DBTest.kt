package com.example.timeroutetracker.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DBTest {

  private lateinit var db: DB
  private lateinit var kvTable: DB.KVTable

  enum class TestEnum {
    VALUE1, VALUE2
  }

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
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
}
