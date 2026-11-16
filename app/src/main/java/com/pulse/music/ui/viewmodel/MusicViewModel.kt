package com.pulse.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.music.data.model.LyricLine
import com.pulse.music.data.model.Track
import com.pulse.music.data.remote.LrcLibService
import com.pulse.music.data.remote.MusicService
import com.pulse.music.playback.PlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(
    val playerManager: PlayerManager,
    private val musicService: MusicService = MusicService(),
    private val lrcLibService: LrcLibService = LrcLibService()
) : ViewModel() {

    val isPlaying = playerManager.isPlaying
    val currentPosition = playerManager.currentPosition
    val duration = playerManager.duration
    val currentTrack = playerManager.currentTrack

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics = _lyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex = _activeLyricIndex.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading = _isLyricsLoading.asStateFlow()

    private val _trendingTracks = MutableStateFlow<List<Track>>(emptyList())
    val trendingTracks = _trendingTracks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var lyricsJob: Job? = null
    private var searchJob: Job? = null

    init {
        _trendingTracks.value = musicService.getTrendingTracks()

        // Track active lyric line based on current playback position
        viewModelScope.launch {
            playerManager.currentPosition.collect { positionMs ->
                val currentList = _lyrics.value
                if (currentList.isNotEmpty()) {
                    val index = currentList.indexOfLast { it.timeMs <= positionMs }
                    _activeLyricIndex.value = index
                }
            }
        }
    }

    fun playTrack(track: Track) {
        playerManager.playTrack(track)
        fetchLyricsForTrack(track)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun searchSongs(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        _isSearching.value = true
        searchJob = viewModelScope.launch {
            val results = musicService.searchSongs(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    private fun fetchLyricsForTrack(track: Track) {
        lyricsJob?.cancel()
        _lyrics.value = emptyList()
        _activeLyricIndex.value = -1
        _isLyricsLoading.value = true

        lyricsJob = viewModelScope.launch {
            val durationSec = if (track.durationMs > 0) track.durationMs / 1000L else null
            val fetchedLyrics = lrcLibService.fetchSyncedLyrics(
                trackTitle = track.title,
                artistName = track.artist,
                durationSeconds = durationSec
            )
            _lyrics.value = fetchedLyrics
            _isLyricsLoading.value = false
        }
    }
}
