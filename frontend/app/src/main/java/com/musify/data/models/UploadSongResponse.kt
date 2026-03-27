package com.musify.data.models

data class UploadSongResponse(
    val message: String,
    val songId: Int,
    val filePath: String
)