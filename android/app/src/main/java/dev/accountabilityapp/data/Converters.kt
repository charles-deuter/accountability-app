package dev.accountabilityapp.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalTime

class Converters {
  @TypeConverter fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilli()

  @TypeConverter fun epochMillisToInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::ofEpochMilli)

  @TypeConverter fun localTimeToSecondOfDay(localTime: LocalTime?): Int? = localTime?.toSecondOfDay()

  @TypeConverter fun secondOfDayToLocalTime(secondOfDay: Int?): LocalTime? = secondOfDay?.let({ seconds -> LocalTime.ofSecondOfDay(seconds.toLong()) })
}
