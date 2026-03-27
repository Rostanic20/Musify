package com.musify.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UserQueues : IntIdTable() {
    val userId = reference("user_id", Users)
    val currentSongId = reference("current_song_id", Songs).nullable()
    val currentPosition = integer("current_position").default(0) // seconds into current song
    val repeatMode = varchar("repeat_mode", 20).default("none") // none, one, all
    val shuffleEnabled = bool("shuffle_enabled").default(false)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

object QueueItems : IntIdTable() {
    val userId = reference("user_id", Users)
    val songId = reference("song_id", Songs)
    val position = integer("position") // order in queue
    val addedAt = datetime("added_at").defaultExpression(CurrentDateTime)
    val sourceType = varchar("source_type", 50).nullable() // playlist, album, search, etc.
    val sourceId = integer("source_id").nullable() // ID of playlist/album if applicable
}
