package dev.accountabilityapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun aVersionOneDatabase_isRebuiltEmpty_ratherThanCrashingTheApp() = runTest(testBody = { ->
    val fileName = "version-one.db"
    context.deleteDatabase(fileName)
    writeVersionOneDatabase(fileName)

    val database = AppDatabase.open(context, fileName)
    val taskList = database.taskDao().observeTaskList().first()
    database.close()

    assertEquals(emptyList<Task>(), taskList)
  })

  /** The schema in app/schemas/.../1.json, with one Task in it, as an install from before the Snooze rework has it. */
  private fun writeVersionOneDatabase(fileName: String) {
    val file = context.getDatabasePath(fileName)
    file.parentFile?.mkdirs()
    val database = SQLiteDatabase.openOrCreateDatabase(file, null)
    database.execSQL(
      "CREATE TABLE `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, " +
        "`scheduledTime` INTEGER NOT NULL, `templateId` INTEGER, `status` TEXT NOT NULL, `snoozeCount` INTEGER NOT NULL, " +
        "`snoozedUntil` INTEGER, `completedAt` INTEGER, `createdAt` INTEGER NOT NULL)"
    )
    database.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    database.execSQL("INSERT INTO room_master_table (id,identity_hash) VALUES(42, 'aa2727a9f40e1052606a7a0300c58200')")
    database.execSQL(
      "INSERT INTO tasks (title, scheduledTime, status, snoozeCount, snoozedUntil, createdAt) " +
        "VALUES ('Take meds', 1761300000000, 'SNOOZED', 1, 1761303600000, 1761290000000)"
    )
    database.version = 1
    database.close()
  }
}
