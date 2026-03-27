package com.musify.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Playlists : IntIdTable() {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val userId = reference("user_id", Users)
    val coverArt = varchar("cover_art", 500).nullable()
    val isPublic = bool("is_public").default(true)
    val isCollaborative = bool("is_collaborative").default(false)
    val collaborativeToken = varchar("collaborative_token", 255).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

object PlaylistSongs : IntIdTable() {
    val playlistId = reference("playlist_id", Playlists)
    val songId = reference("song_id", Songs)
    val position = integer("position")
    val addedAt = datetime("added_at").defaultExpression(CurrentDateTime)
}
