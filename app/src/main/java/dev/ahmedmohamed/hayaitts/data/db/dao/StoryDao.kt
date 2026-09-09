package dev.ahmedmohamed.hayaitts.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.ahmedmohamed.hayaitts.data.db.entities.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun byId(id: Long): StoryEntity?

    @Insert
    suspend fun insert(entity: StoryEntity): Long

    @Query("UPDATE stories SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
