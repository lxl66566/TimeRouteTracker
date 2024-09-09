package com.example.timeroutetracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.timeroutetracker.utils.MySerde

val DATABASE_VERSION = 1

/*
 * 数据库抽象层，提供了 Table 的内部结构
 */
class DB(public val context: Context, databaseName: String = "timeroutetracker.db") {
  private val dbHelper: DBHelper = DBHelper(context, databaseName)

  class DBHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
      // Create tables here if needed
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      // Handle database upgrades
    }
  }

  fun table(tableName: String): Table {
    return Table(tableName)
  }

  inner class Table(private val tableName: String) {
    fun add(values: ContentValues) {
      dbHelper.writableDatabase.use { db ->
        db.insert(tableName, null, values)
      }
    }

    fun delete(whereClause: String, whereArgs: Array<String>) {
      dbHelper.writableDatabase.use { db ->
        db.delete(tableName, whereClause, whereArgs)
      }
    }

    fun update(values: ContentValues, whereClause: String, whereArgs: Array<String>) {
      dbHelper.writableDatabase.use { db ->
        db.update(tableName, values, whereClause, whereArgs)
      }
    }

    fun query(
      columns: Array<String>,
      selection: String?,
      selectionArgs: Array<String>?,
      groupBy: String?,
      having: String?,
      orderBy: String?
    ): Cursor {
      return dbHelper.readableDatabase.query(
        tableName,
        columns,
        selection,
        selectionArgs,
        groupBy,
        having,
        orderBy
      )
    }
  }

  /*
   * Get the KV Table
   */
  fun kvTable(tableName: String): KVTable {
    return KVTable(tableName)
  }

  open inner class KVTable(private val tableName: String) {
    init {
      createTable()
      createIndex()
      assert(validateTableColumns())
    }

    private fun createTable() {
      dbHelper.writableDatabase.use { db ->
        val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        key TEXT PRIMARY KEY,
                        value TEXT
                    )
                """
        db.execSQL(createTableSQL)
      }
    }

    private fun createIndex() {
      dbHelper.writableDatabase.use { db ->
        val createIndexSQL = """
                    CREATE INDEX IF NOT EXISTS idx_key ON $tableName (key)
                """
        db.execSQL(createIndexSQL)
      }
    }

    private fun validateTableColumns(): Boolean {
      val db = dbHelper.readableDatabase
      val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
      val columns = mutableSetOf<String>()

      if (cursor.moveToFirst()) {
        do {
          val temp = cursor.getColumnIndex("name")
          assert(temp >= 0)
          val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
          columns.add(columnName)
        } while (cursor.moveToNext())
      }
      cursor.close()

      return columns.size == 2 && columns.containsAll(setOf("key", "value"))
    }

    fun put(key: String, value: String) {
      val values = ContentValues().apply {
        put("key", key)
        put("value", value)
      }
      dbHelper.writableDatabase.use { db ->
        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
      }
    }

    fun get(key: String): String? {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("value"),
        "key = ?",
        arrayOf(key),
        null,
        null,
        null
      )
      return cursor.use {
        if (it.moveToFirst()) {
          it.getString(it.getColumnIndexOrThrow("value"))
        } else {
          null
        }
      }
    }

    fun delete(key: String) {
      dbHelper.writableDatabase.use { db ->
        db.delete(tableName, "key = ?", arrayOf(key))
      }
    }

    fun query(selection: String?, selectionArgs: Array<String>?): Cursor {
      return dbHelper.readableDatabase.query(
        tableName,
        null,
        selection,
        selectionArgs,
        null,
        null,
        null
      )
    }

    fun getOrInit(key: String, default: String): String {
      val value = get(key)
      return if (value != null) {
        value
      } else {
        put(key, default)
        default
      }
    }

    fun getAll(): HashMap<String, String> {
      val cursor = query(null, null)
      return cursor.use {
        val result = hashMapOf<String, String>()
        while (it.moveToNext()) {
          result[it.getString(it.getColumnIndexOrThrow("key"))] =
            it.getString(it.getColumnIndexOrThrow("value"))
        }
        result
      }
    }

    inline fun <reified T> putAny(key: String, value: T) {
      put(key, MySerde.serialize(value))
    }

    inline fun <reified T> getAny(key: String): T? {
      return get(key)?.let { MySerde.deserialize(it) }
    }

    inline fun <reified T> getOrInitAny(key: String, defaultValue: T): T {
      val value = getAny<T>(key)
      return if (value != null) {
        value
      } else {
        putAny(key, defaultValue)
        defaultValue
      }
    }
  }
}

