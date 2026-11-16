package com.pulse.music.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.music.data.model.LyricLine
import com.pulse.music.ui.theme.LyricActive
import com.pulse.music.ui.theme.LyricInactive
import com.pulse.music.ui.theme.PrimaryLumi
import com.pulse.music.ui.theme.TextMuted
import com.pulse.music.ui.theme.TextSecondary

@Composable
fun LyricsScreen(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    isLoading: Boolean,
    onLyricClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smoothly scroll to the active line when it changes
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && activeLyricIndex < lyrics.size) {
            val targetScroll = maxOf(0, activeLyricIndex - 2)
            listState.animateScrollToItem(
                index = targetScroll,
                scrollOffset = -100
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryLumi,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Syncing live lyrics with melody...",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }

            lyrics.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "♪ No Synced Lyrics Available ♪",
                        color = TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enjoy the sound or search another track",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 180.dp, bottom = 220.dp, start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeLyricIndex

                        val textColor by animateColorAsState(
                            targetValue = if (isActive) LyricActive else LyricInactive,
                            animationSpec = tween(durationMillis = 250),
                            label = "lyric_color"
                        )

                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.05f else 0.98f,
                            animationSpec = tween(durationMillis = 250),
                            label = "lyric_scale"
                        )

                        Text(
                            text = line.text.ifBlank { "• • •" },
                            color = textColor,
                            fontSize = if (isActive) 24.sp else 19.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .clickable {
                                    onLyricClick(line.timeMs)
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
