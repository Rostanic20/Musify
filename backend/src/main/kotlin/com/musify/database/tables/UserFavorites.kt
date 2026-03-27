package com.musify.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UserFavorites : IntIdTable() {
    val userId = reference("user_id", Users)
    val songId = reference("song_id", Songs)
    val addedAt = datetime("added_at").defaultExpression(CurrentDateTime)
}

object ListeningHistory : IntIdTable() {
    val userId = reference("user_id", Users)
    val songId = reference("song_id", Songs)
    val playedAt = datetime("played_at").defaultExpression(CurrentDateTime)
    val playDuration = integer("play_duration") // seconds played
}
