package dev.accountabilityapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao

  companion object {
    const val FILE_NAME = "accountability.db"

    /**
     * The on-disk database the app uses; Tasks persist across process restarts. Version 1 predates any release, so it
     * is dropped rather than migrated. Any later version without a migration still fails loudly.
     */
    fun open(context: Context, fileName: String = FILE_NAME): AppDatabase =
      Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, fileName)
        .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
        .build()
  }
}
