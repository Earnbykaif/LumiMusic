package com.pulse.music.data.remote

import com.pulse.music.data.model.LyricLine
import com.pulse.music.data.model.LrcResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class LrcLibService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val lrcLinePattern = Pattern.compile("""^\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)$""")

    suspend fun fetchSyncedLyrics(trackTitle: String, artistName: String, durationSeconds: Long? = null): List<LyricLine> = withContext(Dispatchers.IO) {
        val cleanTitle = cleanTrackTitle(trackTitle)
        val cleanArtist = artistName.split(",", "&", "feat", "ft.", ";").first().trim()

        // 1. Try exact match /api/get
        val directUrlBuilder = "https://lrclib.net/api/get".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("track_name", cleanTitle)
            ?.addQueryParameter("artist_name", cleanArtist)

        if (durationSeconds != null && durationSeconds > 0) {
            directUrlBuilder?.addQueryParameter("duration", durationSeconds.toString())
        }

        directUrlBuilder?.build()?.let { url ->
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "PulseMusicApp/1.0.0 (https://github.com/pulse-music)")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        response.body?.string()?.let { bodyString ->
                            val lrcResponse = json.decodeFromString<LrcResponse>(bodyString)
                            if (!lrcResponse.syncedLyrics.isNullOrBlank()) {
                                return@withContext parseLrc(lrcResponse.syncedLyrics)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore and fallback to search
            }
        }

        // 2. Fallback to /api/search
        val searchUrl = "https://lrclib.net/api/search".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("q", "$cleanTitle $cleanArtist")
            ?.build() ?: return@withContext emptyList()

        val searchRequest = Request.Builder()
            .url(searchUrl)
            .header("User-Agent", "PulseMusicApp/1.0.0 (https://github.com/pulse-music)")
            .build()

        try {
            client.newCall(searchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { bodyString ->
                        val results = json.decodeFromString<List<LrcResponse>>(bodyString)
                        val matchWithSynced = results.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
                        if (matchWithSynced?.syncedLyrics != null) {
                            return@withContext parseLrc(matchWithSynced.syncedLyrics)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        emptyList()
    }

    fun parseLrc(lrcText: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val rawLines = lrcText.lines()

        for (raw in rawLines) {
            val matcher = lrcLinePattern.matcher(raw.trim())
            if (matcher.matches()) {
                val minStr = matcher.group(1) ?: "00"
                val secStr = matcher.group(2) ?: "00"
                val msStr = matcher.group(3) ?: "00"
                val text = matcher.group(4)?.trim() ?: ""

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L
                val millis = if (msStr.length == 2) {
                    (msStr.toLongOrNull() ?: 0L) * 10
                } else {
                    msStr.toLongOrNull() ?: 0L
                }

                val totalTimeMs = (minutes * 60 + seconds) * 1000 + millis
                lines.add(LyricLine(timeMs = totalTimeMs, text = text))
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    private fun cleanTrackTitle(title: String): String {
        return title
            .replace(Regex("""\([^)]*(official|video|audio|lyrics|hd|4k)[^)]*\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[[^\]]*(official|video|audio|lyrics|hd|4k)[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""(official video|official audio|lyric video|music video)""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
