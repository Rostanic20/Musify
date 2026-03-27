package com.musify.data.mapper

import com.musify.data.models.Album as DataAlbum
import com.musify.data.models.Artist as DataArtist
import com.musify.data.models.Playlist as DataPlaylist
import com.musify.data.models.Song as DataSong
import com.musify.data.models.SongDetails as DataSongDetails
import com.musify.domain.entity.Album
import com.musify.domain.entity.Artist
import com.musify.domain.entity.Playlist
import com.musify.domain.entity.Song

/**
 * Mapper to convert between data layer and domain layer music models
 */
object SongMapper {
    
    fun DataSong.toDomainModel(): Song {
        return Song(
            id = id,
            title = title,
            artist = Artist(
                id = artistId,
                name = artistName,
                bio = null,
                profileImageUrl = null,
                isVerified = false,
                monthlyListeners = 0,
                followersCount = 0
            ),
            album = albumId?.let { albumId ->
                Album(
                    id = albumId,
                    title = albumTitle ?: "",
                    artist = Artist(
                        id = artistId,
                        name = artistName,
                        bio = null,
                        profileImageUrl = null,
                        isVerified = false,
                        monthlyListeners = 0,
                        followersCount = 0
                    ),
                    coverArtUrl = coverArt,
                    releaseYear = 0,
                    trackCount = 0,
                    genre = genre
                )
            },
            durationSeconds = duration,
            coverArtUrl = coverArt,
            genre = genre,
            playCount = playCount,
            isFavorite = false
        )
    }
    
    fun DataSongDetails.toDomainModel(): Song {
        return Song(
            id = song.id,
            title = song.title,
            artist = artist.toDomainModel(),
            album = album?.toDomainModel(),
            durationSeconds = song.duration,
            coverArtUrl = song.coverArt,
            genre = song.genre,
            playCount = song.playCount,
            isFavorite = isFavorite
        )
    }
    
    fun DataArtist.toDomainModel(): Artist {
        return Artist(
            id = id,
            name = name,
            bio = bio,
            profileImageUrl = profileImage,
            isVerified = isVerified,
            monthlyListeners = monthlyListeners,
            followersCount = followersCount
        )
    }
    
    fun DataAlbum.toDomainModel(): Album {
        return Album(
            id = id,
            title = title,
            artist = Artist(
                id = artistId,
                name = artistName,
                bio = null,
                profileImageUrl = null,
                isVerified = false,
                monthlyListeners = 0,
                followersCount = 0
            ),
            coverArtUrl = coverArt,
            releaseYear = releaseDate.take(4).toIntOrNull() ?: 0,
            trackCount = trackCount,
            genre = genre
        )
    }
    
    fun DataPlaylist.toDomainModel(): Playlist {
        return Playlist(
            id = id,
            name = name,
            description = description,
            ownerId = userId,
            ownerName = ownerName ?: "Unknown",
            isPublic = isPublic,
            isCollaborative = isCollaborative,
            coverImageUrl = coverImage,
            songCount = songCount,
            totalDurationSeconds = totalDuration,
            followersCount = followersCount
        )
    }
}