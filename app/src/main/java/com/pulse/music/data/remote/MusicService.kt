package com.pulse.music.data.remote

import com.pulse.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MusicService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // Popular default tracks for instant playback on launch
    fun getTrendingTracks(): List<Track> {
        return listOf(
            Track(
                id = "trending_1",
                title = "Starboy",
                artist = "The Weeknd, Daft Punk",
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                durationMs = 230000L
            ),
            Track(
                id = "trending_2",
                title = "Blinding Lights",
                artist = "The Weeknd",
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                durationMs = 200000L
            ),
            Track(
                id = "trending_3",
                title = "Shape of You",
                artist = "Ed Sheeran",
                artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                durationMs = 233000L
            ),
            Track(
                id = "trending_4",
                title = "Stay",
                artist = "The Kid LAROI, Justin Bieber",
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                durationMs = 141000L
            ),
            Track(
                id = "trending_5",
                title = "Levitating",
                artist = "Dua Lipa",
                artworkUrl = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                durationMs = 203000L
            )
        )
    }

    suspend fun searchSongs(query: String): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // We query Piped instance API for YouTube Music results
        val instances = listOf("https://pipedapi.kavin.rocks", "https://api.piped.privacydev.net")
        for (base in instances) {
            try {
                val encodedQuery = URLEncoder.encode("$query song", "UTF-8")
                val url = "$base/search?q=$encodedQuery&filter=music_songs"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "PulseMusic/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@use
                        val parsed = json.parseToJsonElement(body).jsonObject
                        val items = parsed["items"]?.jsonArray ?: return@use

                        val tracks = mutableListOf<Track>()
                        for (item in items) {
                            val obj = item.jsonObject
                            val urlPath = obj["url"]?.jsonPrimitive?.content ?: ""
                            val videoId = urlPath.substringAfter("watch?v=", "")
                            val title = obj["title"]?.jsonPrimitive?.content ?: "Unknown Track"
                            val uploaderName = obj["uploaderName"]?.jsonPrimitive?.content ?: "Artist"
                            val thumbnail = obj["thumbnail"]?.jsonPrimitive?.content ?: ""
                            val durationSec = obj["duration"]?.jsonPrimitive?.content?.toLongOrNull() ?: 180L

                            if (videoId.isNotBlank()) {
                                tracks.add(
                                    Track(
                                        id = videoId,
                                        title = title,
                                        artist = uploaderName,
                                        artworkUrl = thumbnail,
                                        // Piped audio stream endpoint
                                        streamUrl = "$base/streams/$videoId",
                                        durationMs = durationSec * 1000L
                                    )
                                )
                            }
                        }
                        if (tracks.isNotEmpty()) {
                            return@withContext tracks
                        }
                    }
                }
            } catch (e: Exception) {
                // Try next instance
            }
        }

        // Fallback search filter from trending
        getTrendingTracks().filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
    }
}
