package dev.accountabilityapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao

  companion object {
    const val FILE_NAME = "accountability.db"

    /** The on-disk database the app uses; Tasks persist across process restarts. */
    fun open(context: Context, fileName: String = FILE_NAME): AppDatabase =
      Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, fileName).build()
  }
}
