package dev.accountabilityapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TemplateDao {
  @Insert suspend fun insert(template: Template): Long

  @Query("SELECT * FROM templates WHERE id = :templateId")
  suspend fun findTemplate(templateId: Long): Template?
}
