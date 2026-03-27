package com.musify.database.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object PlaylistCollaborators : LongIdTable("playlist_collaborators") {
    val playlistId = reference("playlist_id", Playlists)
    val userId = reference("user_id", Users)
    val role = varchar("role", 20).default("contributor")
    val canAddSongs = bool("can_add_songs").default(true)
    val canRemoveSongs = bool("can_remove_songs").default(true)
    val canInviteOthers = bool("can_invite_others").default(false)
    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp())

    init {
        uniqueIndex(playlistId, userId)
    }
}
