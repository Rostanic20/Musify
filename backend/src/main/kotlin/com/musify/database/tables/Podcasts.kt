package com.musify.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object PodcastShows : IntIdTable() {
    val title = varchar("title", 255)
    val description = text("description")
    val author = varchar("author", 255)
    val coverArt = varchar("cover_art", 500).nullable()
    val category = varchar("category", 100)
    val language = varchar("language", 10).default("en")
    val rssUrl = varchar("rss_url", 500).nullable()
    val websiteUrl = varchar("website_url", 500).nullable()
    val explicit = bool("explicit").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

object PodcastEpisodes : IntIdTable() {
    val showId = reference("show_id", PodcastShows)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val audioUrl = varchar("audio_url", 500)
    val duration = integer("duration") // in seconds
    val episodeNumber = integer("episode_number").nullable()
    val season = integer("season").nullable()
    val publishedAt = datetime("published_at").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object PodcastSubscriptions : IntIdTable() {
    val userId = reference("user_id", Users)
    val showId = reference("show_id", PodcastShows)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object PodcastProgress : IntIdTable() {
    val userId = reference("user_id", Users)
    val episodeId = reference("episode_id", PodcastEpisodes)
    val position = integer("position") // seconds into episode
    val completed = bool("completed").default(false)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
