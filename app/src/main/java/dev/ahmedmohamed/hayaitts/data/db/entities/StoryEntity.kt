package dev.ahmedmohamed.hayaitts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One imported text file. The text lives on disk at [path]; only metadata is
 * stored here. Added in DB schema v5.
 */
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val sizeBytes: Long,
    val importedAt: Long,
    val path: String,
)
