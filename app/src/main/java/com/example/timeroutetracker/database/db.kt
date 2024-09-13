package com.example.timeroutetracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.example.timeroutetracker.utils.InvalidSqliteException
import com.example.timeroutetracker.utils.MySerde
import com.example.timeroutetracker.utils.TimeSpan
import com.example.timeroutetracker.utils.Validation
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime

const val DATABASE_VERSION = 1


/*
 * 提供了数据库获取上下文的抽象，用于其他 interface
 */
interface GetMembers {
  fun getContext(): Context
  fun getDatabaseName(): String
}

/*
 * 提供了数据库的备份和恢复抽象
 */
interface BackupableDb : GetMembers {
  fun exportDatabaseToUri(uri: Uri): Boolean {
    val databaseFile = getContext().getDatabasePath(getDatabaseName())
    getContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
      FileInputStream(databaseFile).use { inputStream ->
        copyStream(inputStream, outputStream)
      }
    }
    return File(uri.path!!).exists()
  }

  /*
   * 从 uri 导入数据库
   * 返回 false 表示文件格式错误
   */
  fun importDatabaseFromUri(uri: Uri) {
    val databaseFile = getContext().getDatabasePath(getDatabaseName())
    if (!Validation.isSqliteFile(databaseFile)) {
      throw InvalidSqliteException("Not a sqlite file")
    }
    getContext().contentResolver.openInputStream(uri)?.use { inputStream ->
      FileOutputStream(databaseFile).use { outputStream ->
        copyStream(inputStream, outputStream)
      }
    }
  }

  private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      outputStream.write(buffer, 0, bytesRead)
    }
  }

  fun deleteDatabase() {
    getContext().deleteDatabase(getDatabaseName())
  }
}


/*
 * 数据库抽象层，提供了 Table 的内部结构
 */
class DB(
  private val context: Context,
  private val databaseName: String = "timeroutetracker.sqlite3"
) :
  BackupableDb {
  override fun getContext(): Context = context
  override fun getDatabaseName(): String = databaseName

  private val dbHelper: DBHelper = DBHelper(context, databaseName)
  private val LocalDateTimeConverter = LocalDateTimeConverter()
  private val DurationConverter = DurationConverter()
  private val LatLngConverter = LatLngConverter()

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

  open inner class routeTable(private val tableName: String = "Route") {
    init {
      createTable()
      createIndex()
    }

    private fun createTable() {
      dbHelper.writableDatabase.use { db ->
        val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        time INTEGER,
                        location BLOB
                    )
                """
        db.execSQL(createTableSQL)
      }
    }

    private fun createIndex() {
      dbHelper.writableDatabase.use { db ->
        val createIndexSQL = """
                    CREATE INDEX IF NOT EXISTS idx_time ON $tableName (time)
                """
        db.execSQL(createIndexSQL)
      }
    }

    fun insertRouteItem(item: RouteItem) {
      val values = ContentValues().apply {
        put("time", LocalDateTimeConverter.from(item.time))
        put("location", LatLngConverter.from(item.location))
      }
      dbHelper.writableDatabase.use { db ->
        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
      }
    }

    fun getSpan(start: LocalDateTime, end: LocalDateTime): MutableList<LatLng> {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("location"),
        "time >= ? AND time <= ?",
        arrayOf(
          LocalDateTimeConverter.from(start).toString(),
          LocalDateTimeConverter.from(end).toString()
        ),
        null,
        null,
        "time ASC"
      )
      return cursor.use {
        val result = mutableListOf<LatLng>()
        while (it.moveToNext()) {
          result.add(LatLngConverter.to(it.getBlob(it.getColumnIndexOrThrow("location"))))
        }
        result
      }
    }

    fun getSpan(span: TimeSpan): MutableList<LatLng> {
      return getSpan(span.start, span.end)
    }
  }

  open inner class categoryTable(private val tableName: String = "CategoryInfo") {
    init {
      createTable()
    }

    private fun createTable() {
      dbHelper.writableDatabase.use { db ->
        val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        categoryId INTEGER PRIMARY KEY AUTOINCREMENT,
                        categoryName TEXT UNIQUE,
                        categoryColor INTEGER
                    )
                """
        db.execSQL(createTableSQL)
      }
    }

    fun insertCategory(category: CategoryInfo) {
      dbHelper.writableDatabase.use { db ->
        val values = ContentValues().apply {
          put("categoryName", category.categoryName)
          put("categoryColor", category.categoryColor)
        }
        val selection = "categoryName = ?"
        val selectionArgs = arrayOf(category.categoryName)
        val count = db.update(tableName, values, selection, selectionArgs)
        if (count == 0) {
          db.insert(tableName, null, values)
        }
      }
    }

    fun getCategoryList(): List<CategoryInfoWithId> {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("categoryId", "categoryName", "categoryColor"),
        null,
        null,
        null,
        null,
        null
      )
      return cursor.use {
        val result = mutableListOf<CategoryInfoWithId>()
        while (it.moveToNext()) {
          result.add(
            CategoryInfoWithId(
              it.getInt(it.getColumnIndexOrThrow("categoryId")),
              it.getString(it.getColumnIndexOrThrow("categoryName")),
              it.getInt(it.getColumnIndexOrThrow("categoryColor"))
            )
          )
        }
        result
      }

    }

    fun getCategoryById(id: Int): CategoryInfo? {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("categoryName", "categoryColor"),
        "categoryId = ?",
        arrayOf(id.toString()),
        null,
        null,
        null
      )
      return cursor.use {
        if (it.moveToNext()) {
          CategoryInfo(
            it.getString(it.getColumnIndexOrThrow("categoryName")),
            it.getInt(it.getColumnIndexOrThrow("categoryColor"))
          )
        } else {
          null
        }
      }
    }
  }

  open inner class appInfoTable(
    private val tableName: String = "AppInfo",
    private val categoryTableName: String = "CategoryInfo"
  ) {
    init {
      createTable()
      createIndex()
    }

    private fun createTable() {
      dbHelper.writableDatabase.use { db ->
        val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        packageName TEXT NOT NULL UNIQUE,
                        appName TEXT,
                        categoryId INTEGER
                        FOREIGN KEY (categoryId) REFERENCES $categoryTableName (categoryId)
                    )
                """
        db.execSQL(createTableSQL)
      }
    }

    private fun createIndex() {
      dbHelper.writableDatabase.use { db ->
        val createIndexSQL = """
                    CREATE INDEX IF NOT EXISTS idx_packageName ON $tableName (packageName)
                """
        db.execSQL(createIndexSQL)
      }
    }

    fun insertAppInfo(appInfo: AppInfo) {
      dbHelper.writableDatabase.use { db ->
        val values = ContentValues().apply {
          put("packageName", appInfo.packageName)
          put("appName", appInfo.appName)
          put("categoryId", appInfo.categoryId)
        }
        val selection = "packageName = ?"
        val selectionArgs = arrayOf(appInfo.packageName)
        val count = db.update(tableName, values, selection, selectionArgs)
        if (count == 0) {
          db.insert(tableName, null, values)
        }
      }
    }

    fun getAppInfoByPackage(packageName: String): AppInfo? {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("packageName", "appName", "categoryId"),
        "packageName = ?",
        arrayOf(packageName),
        null,
        null,
        null
      )
      return cursor.use {
        if (it.moveToNext()) {
          AppInfo(
            it.getString(it.getColumnIndexOrThrow("packageName")),
            it.getString(it.getColumnIndexOrThrow("appName")),
            it.getInt(it.getColumnIndexOrThrow("categoryId"))
          )
        } else {
          null
        }
      }
    }
  }


  open inner class timeTable(
    private val tableName: String,
    private val appInfoTableName: String = "AppInfo"
  ) {
    init {
      createTable()
      createIndex()
    }

    private fun createTable() {
      dbHelper.writableDatabase.use { db ->
        val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        recordId INTEGER PRIMARY KEY AUTOINCREMENT,
                        appId INTEGER NOT NULL,
                        time INTEGER NOT NULL, -- 持续时间以毫秒为单位
                        datetime INTEGER NOT NULL, -- 开始时间以时间戳形式存储
                        FOREIGN KEY (appId) REFERENCES $appInfoTableName (appId)
                    )
                """
        db.execSQL(createTableSQL)
      }
    }

    private fun createIndex() {
      dbHelper.writableDatabase.use { db ->
        val createIndexSQL = """
                    CREATE INDEX IF NOT EXISTS idx_datetime ON $tableName (datetime)
                """
        db.execSQL(createIndexSQL)
      }
    }

    fun insertAppTimeRecord(insertion: AppTimeInsertion) {
      dbHelper.writableDatabase.use { db ->
        val values = ContentValues().apply {
          put("time", DurationConverter.from(insertion.time)) // 假设时间是以毫秒为单位
          put("datetime", LocalDateTimeConverter.from(insertion.datetime)) // 时间戳格式
        }

        // 查询 appId
        val query = """
            SELECT appId 
            FROM AppInfo 
            WHERE packageName = ?
        """
        val cursor = db.rawQuery(query, arrayOf(insertion.packageName))

        if (cursor.moveToFirst()) {
          val appId = cursor.getInt(cursor.getColumnIndexOrThrow("appId"))
          cursor.close()

          // 插入记录
          values.put("appId", appId)
          db.insert("AppTimeRecord", null, values)
        } else {
          cursor.close()
          // 处理没有找到对应 appId 的情况
          throw IllegalArgumentException("Package name ${insertion.packageName} not found in AppInfo.")
        }
      }
    }


    fun getTimeRecord(appId: Int, datetime: LocalDateTime): AppTimeRecord? {
      val cursor = dbHelper.readableDatabase.query(
        tableName,
        arrayOf("time"),
        "appId = ? AND datetime = ?",
        arrayOf(appId.toString(), LocalDateTimeConverter.from(datetime).toString()),
        null,
        null,
        null
      )
      return cursor.use {
        if (it.moveToNext()) {
          AppTimeRecord(
            appId,
            DurationConverter.to(it.getLong(it.getColumnIndexOrThrow("time"))),
            LocalDateTimeConverter.to(it.getLong(it.getColumnIndexOrThrow("datetime")))
          )
        } else {
          null
        }
      }
    }
  }
}
