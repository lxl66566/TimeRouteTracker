package com.example.timeroutetracker.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DBTest {

  private lateinit var db: DB
  private lateinit var kvTable: DB.KVTable

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    db = DB(context, "test_database")
    kvTable = db.kvTable("test_kv_table")
  }

  @Test
  fun testGetOrInit() {
    // Ensure the table is empty
    kvTable.delete("test_key")

    // Use getOrInit and check default value insertion
    val defaultValue = "default_value"
    val value = kvTable.getOrInit("test_key", defaultValue)
    assertEquals("Value should be the default value", defaultValue, value)

    // Retrieve the value again to ensure it was correctly inserted
    val retrievedValue = kvTable.get("test_key")
    assertEquals("Retrieved value should match the default value", defaultValue, retrievedValue)

    // Update the value and check if the new value is retrieved
    val newValue = "new_value"
    kvTable.put("test_key", newValue)
    val updatedValue = kvTable.getOrInit("test_key", defaultValue)
    assertEquals("Updated value should be returned, not the default value", newValue, updatedValue)
  }

}
