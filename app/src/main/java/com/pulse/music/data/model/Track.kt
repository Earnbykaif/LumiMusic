package com.pulse.music.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val streamUrl: String,
    val durationMs: Long = 0L
)
