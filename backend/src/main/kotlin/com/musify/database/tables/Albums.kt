package com.musify.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDate
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object Albums : IntIdTable() {
    val title = varchar("title", 255)
    val artistId = reference("artist_id", Artists)
    val coverArt = varchar("cover_art", 500).nullable()
    val releaseDate = date("release_date").defaultExpression(CurrentDate)
    val genre = varchar("genre", 100).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
