# 🎵 PulseMusic

**PulseMusic** is an open-source Android music streaming player built with **Kotlin** and **Jetpack Compose**, featuring **real-time synchronized lyrics (Karaoke style)** powered by LRCLIB, high-fidelity audio playback via AndroidX Media3 (ExoPlayer), and background playback services.

---

## ✨ Features

- **🎤 Real-Time Synced Lyrics (Karaoke Style):**
  - Powered by the free and open-source **LRCLIB** engine.
  - Sub-second timestamp synchronization with active line glow and auto-scrolling.
  - **Tap-to-Seek:** Tap on any lyric line to instantly jump the song playback to that timestamp!
- **🎧 Seamless Streaming:**
  - Online music search and streaming without advertisements.
  - Instant pre-loaded trending tracks on launch.
- **📱 Modern Jetpack Compose UI:**
  - AMOLED dark mode with sleek gradients and smooth spring animations.
  - Interactive bottom mini-player expanding into a full-screen now-playing screen.
  - Quick toggle button between Album Art and Live Lyrics.
- **🔄 Background Playback & Lock Screen:**
  - AndroidX Media3 `MediaSessionService` support with system media notification and lock screen controls.

---

## 🛠️ Tech Stack & Architecture

- **UI:** Jetpack Compose + Material 3
- **Audio Engine:** `androidx.media3:media3-exoplayer` + `androidx.media3:media3-session`
- **Synced Lyrics:** LRCLIB API (`https://lrclib.net`)
- **Networking:** OkHttp 4 + Kotlinx Serialization
- **Image Loading:** Coil for Compose
- **Architecture:** Clean MVVM with Kotlin Coroutines & StateFlow

---

## 📦 How to Build the APK

### Method 1: Automatic 1-Click Cloud Build (GitHub Actions - Recommended)
You don't need Android Studio or SDK installed on your PC to get the APK:
1. Initialize a git repository and push this project to your GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of PulseMusic"
   git remote add origin https://github.com/<your-username>/PulseMusic.git
   git push -u origin main
   ```
2. Go to the **Actions** tab in your GitHub repository.
3. The **Build PulseMusic APK** workflow will run automatically and produce `app-debug.apk`.
4. Download the APK artifact and install it directly onto your Android phone!

---

### Method 2: Local Build via Android Studio
1. Open **Android Studio** (Hedgehog / Iguana or later with JDK 17+).
2. Choose **Open** and select this directory (`PulseMusic`).
3. Allow Gradle to sync.
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** or click the green **Run** button to install on a connected phone or emulator.
5. The generated APK will be at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 📂 Project Structure

```
PulseMusic/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/pulse/music/
│   │   │   ├── MainActivity.kt
│   │   │   ├── PulseApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── model/ (Track, LyricLine, LrcResponse)
│   │   │   │   └── remote/ (LrcLibService, MusicService)
│   │   │   ├── playback/ (PlayerManager, PlaybackService)
│   │   │   └── ui/
│   │   │       ├── components/ (MiniPlayer)
│   │   │       ├── screens/ (HomeScreen, PlayerScreen, LyricsScreen)
│   │   │       ├── theme/ (Color, Theme, Type)
│   │   │       └── viewmodel/ (MusicViewModel)
│   │   └── res/
│   └── build.gradle.kts
├── .github/workflows/build-apk.yml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```
