package com.pulse.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.pulse.music.playback.PlaybackService
import com.pulse.music.ui.components.MiniPlayer
import com.pulse.music.ui.screens.HomeScreen
import com.pulse.music.ui.screens.PlayerScreen
import com.pulse.music.ui.theme.PulseMusicTheme
import com.pulse.music.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val app = application as PulseApplication
        val viewModel = MusicViewModel(app.playerManager)

        // Start media background playback service
        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)

        setContent {
            PulseMusicTheme {
                var isPlayerExpanded by remember { mutableStateOf(false) }

                val currentTrack by viewModel.currentTrack.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val currentPosition by viewModel.currentPosition.collectAsState()
                val duration by viewModel.duration.collectAsState()
                val lyrics by viewModel.lyrics.collectAsState()
                val activeLyricIndex by viewModel.activeLyricIndex.collectAsState()
                val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
                val trendingTracks by viewModel.trendingTracks.collectAsState()
                val searchResults by viewModel.searchResults.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Home & Search Screen
                        HomeScreen(
                            trendingTracks = trendingTracks,
                            searchResults = searchResults,
                            isSearching = isSearching,
                            currentTrackId = currentTrack?.id,
                            onSearch = { query -> viewModel.searchSongs(query) },
                            onTrackSelected = { track ->
                                viewModel.playTrack(track)
                            }
                        )

                        // Floating Mini Player at Bottom
                        if (currentTrack != null && !isPlayerExpanded) {
                            MiniPlayer(
                                track = currentTrack!!,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onClick = { isPlayerExpanded = true },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }

                        // Full Expanded Player Screen (With Synced Lyrics)
                        AnimatedVisibility(
                            visible = isPlayerExpanded && currentTrack != null,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            PlayerScreen(
                                track = currentTrack!!,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                lyrics = lyrics,
                                activeLyricIndex = activeLyricIndex,
                                isLyricsLoading = isLyricsLoading,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onSeek = { posMs -> viewModel.seekTo(posMs) },
                                onDismiss = { isPlayerExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
