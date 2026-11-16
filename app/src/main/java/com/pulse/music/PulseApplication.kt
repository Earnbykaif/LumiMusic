package com.pulse.music

import android.app.Application
import com.pulse.music.playback.PlayerManager

class PulseApplication : Application() {
    lateinit var playerManager: PlayerManager
        private set

    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        playerManager.release()
    }
}
