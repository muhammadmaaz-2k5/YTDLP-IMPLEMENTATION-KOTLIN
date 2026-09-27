# MediaSaver

<img width="406" height="898" alt="image" src="https://github.com/user-attachments/assets/4dc142b8-965d-49f1-93c5-56b508f83a44" />


A Kotlin Multiplatform (Android-only in practice) video/image downloader. Runs [yt-dlp](https://github.com/yt-dlp/yt-dlp)
and [ffmpeg](https://ffmpeg.org) entirely on-device — no backend server, no remote API of its own.
Paste a link (Facebook, Instagram, YouTube, TikTok, and anything else yt-dlp supports) and it
fetches, previews, and downloads directly from the source site, the same requests a browser would
make.

Not distributed through the Play Store — bundled video-extraction/download tools are consistently
rejected under Play's Device and Network Abuse / IP policies regardless of framing. Sideload only.

## Features

- Paste-a-link download flow with format/quality picker, thumbnail preview, and progress
- Clipboard link auto-detection with a one-tap download prompt, and a share-target entry point
  from other apps
- Background downloads via WorkManager (survive process death) with a foreground-service progress
  notification
- Full download history — search, filter, sort, per-item rename/share/delete
- Light/Dark/System theming, applied consistently across headers and the floating nav bar
- Google AdMob (App Open, Banner, Interstitial, Native, Rewarded, Rewarded Interstitial) with UMP
  consent, gating a local "Premium" feature unlock (watermark-free / audio-only / highest quality)
- No account system, no analytics/crash-reporting SDK, no server — see the in-app Privacy Policy
  for exactly what AdMob itself can see

## Module structure

- `composeApp` — a Kotlin Multiplatform *library* module (`androidMain` only in practice). Domain
  models, repositories, ViewModel, and all Compose UI/screens live here as `commonMain`, with
  Android-specific implementations (`expect`/`actual`) in `androidMain`.
- `androidApp` — a plain `com.android.application` module (required because Chaquopy's Gradle
  plugin doesn't support the KMP library DSL). Hosts the embedded Python runtime running yt-dlp, a
  bundled arm64-v8a ffmpeg binary, navigation (`androidx.navigation`), and the `Application`/
  `Activity` entry points.

Because of the bundled Chaquopy Python runtime and static ffmpeg binary, the APK is built
`arm64-v8a` only.

## Building

```bash
export JAVA_HOME="/path/to/Android Studio/jbr"   # or any JDK 17+
./gradlew :androidApp:assembleDebug
```

APK output: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

## Licenses / third-party notices

- yt-dlp — [Unlicense](https://github.com/yt-dlp/yt-dlp/blob/master/LICENSE) (public domain)
- ffmpeg — this build is compiled with `--disable-gpl --disable-nonfree`, distributed under
  [LGPL-2.1](https://ffmpeg.org/legal.html)

Both are shown in-app under About & Licenses, as required by their respective licenses since
their binaries are redistributed with the app.
