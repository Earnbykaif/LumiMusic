package com.pulse.music.data.model

import kotlinx.serialization.Serializable

data class LyricLine(
    val timeMs: Long,
    val text: String
)

@Serializable
data class LrcResponse(
    val id: Long? = null,
    val name: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean? = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)
