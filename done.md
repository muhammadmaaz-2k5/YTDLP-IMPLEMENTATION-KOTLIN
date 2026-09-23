# MediaSaver — Progress Log

Kotlin Multiplatform (Android-only in practice) video/image downloader. Runs yt-dlp + ffmpeg
entirely on-device via Chaquopy — no backend server, no remote API of its own. Not distributed
through Play Store (that category of app is consistently rejected there); sideload only.

Build verified after every change in this log: `./gradlew :androidApp:assembleDebug` succeeds.
APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

---

## 1. Baseline (already existed before this session)

- `commonMain`/`androidMain` split (`composeApp`) + a plain Android application module
  (`androidApp`) that hosts Chaquopy (Chaquopy's Gradle plugin doesn't support the KMP library
  DSL `composeApp` uses).
- Working fetch → format-select → download → MediaStore-save flow via `YtDlpEngine` /
  `DownloadRepository` / `AppViewModel`.
- JSON-file download history (`HistoryStore`), Material 3 theme (Poppins font, purple/violet
  brand palette).

## 2. UI overhaul (matched to a reference mockup)

Reference was a 3-screen mockup: onboarding, home, downloads-with-tabs — vibrant purple/indigo,
pill inputs/buttons, large rounded cards, bottom-anchored CTA.

- **`OnboardingScreen`** — one-time first-launch screen (purple hero, stacked card graphic, logo
  mark, CTA), gated by `OnboardingStore` (SharedPreferences).
- **`HomeScreen`** rebuilt — header (logo/title/premium button), pill search bar (paste/submit),
  platform quick-open row, "Recently Download" preview card, premium banner, contextual bottom
  CTA (visible only when idle/loading — hidden when a state-specific button already exists).
- **`DownloadsScreen`** — separate full screen: tabs (All/Complete/Failed), grouped by date,
  progress/size display, 3-dot menu.
- **Real thumbnails** — added Coil3 image loading; `MediaPreviewCard` / `RecentDownloadCard` /
  list rows now show actual video/image thumbnails (`mediaInfo.thumbnailUrl` was fetched but
  previously unused).
- `uploader` field wired through from yt-dlp into `MediaInfo` (was extracted by the Python side
  but dropped on the Kotlin side).

## 3. Icons, navigation, premium plans, security pass

- **Real brand icons** — added the Simple Icons pack (`br.com.devsrsouza.compose.icons:simple-icons`,
  CC0) for the platform row, replacing hand-drawn letter/glyph placeholders.
- **Real navigation** — replaced the original hand-rolled enum + `AnimatedContent` switch with
  `androidx.navigation:navigation-compose` (stable, not the alpha multiplatform build): a real
  back stack, working system back button. Lives in `androidApp` (Android-only, stable lib) while
  screens stay in `composeApp` commonMain and take only callback lambdas — no nav dependency
  leaks into shared UI code.
- **Premium plans screen** — Monthly / 3 Months / Yearly, radio selection, per-month-equivalent
  pricing, badges. **No payment processing is wired up, and the screen says so** — Google Play
  Billing needs Play Store distribution, which this app deliberately isn't using. Selecting a
  plan persists locally (`PremiumStore`) so the UI reflects it; ready to swap in a real billing
  SDK if the distribution model ever changes.
- **Security fixes**:
  - Real bug: the API 28 fallback file-sharing path handed out raw `file://` URIs →
    `FileUriExposedException` when shared to another app. Fixed with a proper `FileProvider` +
    `file_paths.xml`.
  - `network_security_config.xml` now denies cleartext HTTP app-wide by default (was previously
    only blocking it for 4 named domains).

## 4. YouTube icon swap + Privacy Policy

- Swapped the TikTok chip for YouTube in the platform row (icon + brand color + package/web
  fallback).
- **`PrivacyPolicyScreen`** — honest, specific to what the app actually does (no server, no
  analytics/ads/crash-reporting, what's stored locally, permissions and why, data deletion).
  Reachable from Home's Info icon → About dialog → "Privacy Policy".

## 5. Phase A — structure & settings (from the larger production-roadmap request)

Scoped deliberately: additive changes that don't touch the already-working download pipeline.
Background downloads / WorkManager / notifications / pause-resume / retry-backoff / storage
management were explicitly **deferred** — see §7.

- **Bottom navigation** — Home / Downloads / Settings, real `NavHostController` back stack,
  standard "don't stack duplicate tabs" pattern.
- **`SettingsScreen`** — Downloads (Wi-Fi only, ask-before-download), Behavior (auto-detect
  clipboard, confirm-before-delete), Appearance (Light/Dark/System — wired into `AppTheme`),
  About (version, Privacy Policy, Terms of Service, Copyright & Content Policy, Contact Support,
  Open Source Licenses). Every toggle is backed by `SettingsStore` and actually enforced
  somewhere (not decorative) — see call sites below.
- **`TermsOfServiceScreen`** — pairs with Privacy Policy (no platform affiliation, no
  access-control bypass, no warranty, open-source component notices).
- **Wi-Fi-only downloads** — real `ConnectivityManager` check in `AppViewModel.onDownloadClick`;
  blocks the download with a clear error if enforced and not on Wi-Fi.
- **Ask-before-download** — confirmation dialog in `HomeScreen` gating the actual download start.
- **Confirm-before-delete** — confirmation dialog in `DownloadsScreen` / `DownloadDetailScreen`
  gating actual file deletion (`deleteDownloadedFile` + `deleteRecordAndFile`).
- **`DownloadDetailScreen`** — large thumbnail, full metadata (platform, duration, resolution,
  fps, size, format, date), Open/Share/Rename/Delete actions. Rename updates both the on-disk
  file (`ContentResolver.update`, MediaStore path only — the rare API-28 FileProvider path
  doesn't support rename and reports failure honestly) and the history record.
- **Search + sort** on `DownloadsScreen` (title/creator/platform search; Newest/Oldest/
  Largest/Smallest sort).
- **Clipboard "Link detected" prompt** — resume-triggered (`MainActivity.onResume` → a bumped
  Int, no polling), respects the auto-detect toggle, dedupes dismissed URLs.

## 6. Clipboard auto-suggest (refined) + Share Sheet integration

- Explained and respected a real OS constraint: Android 10+ blocks background apps from reading
  the clipboard at all (anti-snooping protection) — a true "floating icon the instant you copy a
  link in another app" isn't achievable for a normal app without a fake/non-functional overlay,
  which was deliberately **not** built.
- Upgraded the in-app clipboard prompt from a text banner to a floating **download-icon bubble**
  (animated scale+fade in) — the legitimate, resume-triggered equivalent.
- **Share Sheet integration hardened** — this is the real "recommend our app on these apps"
  mechanism (Instagram/Facebook/YouTube/X/Pinterest's native "Share" already offers MediaSaver
  as a target, no special permission needed):
  - Fixed URL extraction to pull the link out of shared text via regex instead of assuming
    `EXTRA_TEXT` is a bare URL (many apps share a caption + link together).
  - Added `singleTask` launch mode + `onNewIntent` handling so a second share while the app is
    already running updates the same instance instead of spawning a duplicate.
  - Broadened the manifest intent-filter to also catch `text/html` shares.

## 7. Quick Settings tile ("Quick Download")

Answers "can this work even when the app is fully closed?" — the honest answer is that Android's
background-clipboard restriction applies no matter the app's state (closed, backgrounded, even a
foreground service), so nothing can detect a copy event without the app being focused. What *is*
legitimate: removing the friction of getting there.

- **`QuickDownloadTileService`** — a Quick Settings tile (swipe-down panel, reachable from any
  screen and, on most devices, the lock screen) that launches `MainActivity` directly on tap.
  Once launched, the app is focused and the existing resume-based clipboard check runs
  immediately — same detection as before, just reachable in one swipe + one tap instead of
  hunting for the launcher icon.
- Handles the API 34+ `PendingIntent`-based `startActivityAndCollapse` vs. the older `Intent`
  overload for everything below it (compileSdk 36, minSdk 28).
- New: `QuickDownloadTileService.kt`, `res/drawable/ic_tile_download.xml`, a
  `quick_download_tile_label` string, and the `<service>` manifest entry
  (`BIND_QUICK_SETTINGS_TILE`, standard system permission — no user-facing runtime prompt).
- Users add it manually via their Quick Settings panel's edit/pencil icon (standard Android
  behavior for all tiles, not something an app can force).

---

## 8. Background download engine (WorkManager)

The single biggest architectural gap, now closed. Downloads used to run on a coroutine tied to
the Activity's composition scope — any process death (OS memory pressure, a phone call, just
backgrounding long enough) silently killed an in-progress download with no notification and no
way to know it happened. Fixed a real, verified bug along the way (see below) rather than papering
over it.

- **`DownloadWorker`** (`CoroutineWorker`) now runs the actual download — `AppModule.repository`
  (same `DownloadRepository` as before: MediaStore publish + history recording included) is
  called from inside `doWork()` instead of from the ViewModel. `setForeground()` promotes it to a
  foreground service the moment it starts, with `FOREGROUND_SERVICE_TYPE_DATA_SYNC` (API 29+) so
  it isn't reclaimed while active.
- **`DownloadNotifier`** — two channels: `download_progress` (`IMPORTANCE_LOW`, silent, updates
  in place with percent/speed and a working Cancel action via WorkManager's own
  `createCancelPendingIntent`) and `download_status` (`IMPORTANCE_DEFAULT`, one-shot
  complete/failed, tap-to-open on success). Uses the framework's built-in
  `android.R.drawable.stat_sys_download` icon — `composeApp`'s KMP android-library target doesn't
  expose a usable generated `R` class for its own resources the way a classic library module
  does, so a custom drawable wasn't reachable from this module; the built-in icon sidesteps that
  entirely and is genuinely the right icon for this anyway.
- **`WorkManagerDownloadScheduler`** (`DownloadScheduler` interface + androidMain impl, same
  store-pattern as everything else) — `MediaInfo`/`MediaSource` (already `@Serializable`) are
  JSON-encoded into WorkManager's `Data` as the worker's input. One unique work slot
  (`ExistingWorkPolicy.REPLACE`) — matches the current one-at-a-time architecture honestly instead
  of silently allowing a second job to run unmanaged.
- **`AppViewModel.onDownloadClick`/`onCancel`** rewired to enqueue + observe
  `WorkManager.getWorkInfosForUniqueWorkFlow(...)` instead of collecting the repository's Flow
  directly — translates `WorkInfo` state/progress back into the same `DownloadStatus` sealed
  values, so `DownloadProgressBar` and everything else downstream needed zero changes.
- **`POST_NOTIFICATIONS` runtime permission** (API 33+) — was declared in the manifest but never
  actually requested; without a runtime grant, notifications were silently dropped on Android 13+.
  Now requested once per cold start from `AppRoot`, not gated behind starting a download.
- Manifest: `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` permissions, and
  `androidx.work.impl.foreground.SystemForegroundService` overridden with
  `android:foregroundServiceType="dataSync"` via `tools:node="merge"` (required on API 34+ or the
  worker's `setForeground()` call throws) — **verified present and correctly merged** in the built
  APK's manifest, not just assumed from the source.
- **Real bug found and fixed while auditing this**: `AppViewModel.onUrlSubmit` never checked
  whether a download was active, and `UrlInputBar` only disabled itself during extraction
  (`Loading`), not during `Downloading`. Submitting a second URL mid-download didn't cancel or
  queue the first — it orphaned it: the UI jumped to the new URL's preview while the old download
  kept running unmanaged, ready to clobber whatever was on screen the moment it finished or
  failed. Fixed with a two-layer guard (ViewModel no-ops, `UrlInputBar` is disabled) — verified by
  reading the actual code paths, not assumed.
- **Known honest limitation, not glossed over**: if the process dies mid-download, WorkManager can
  re-run the work, but `ChaquopyYtDlpEngine.download()` generates a fresh temp directory
  (`UUID.randomUUID()`) on every call — so a restart begins again from 0%, it doesn't resume via
  yt-dlp's own `.part`-file continuation. That's still a large improvement over silently losing
  the download with no retry at all, but true pause/resume (stable per-job directory + re-enqueue
  targeting the same path) is a small, well-understood, separately-scoped follow-up, not done here.

## 11. Branding, redesign reconciliation, and monetization (undocumented until now)

Several sessions' worth of work landed here without a log entry — catching up in one pass:

- **App icon + splash screen** — new adaptive launcher icon (brand gradient background +
  white download-arrow foreground, both vector, no rasterization needed since minSdk 28 means
  the legacy per-density mipmap fallbacks are never actually used). Branded splash via
  `androidx.core:core-splashscreen` (works pre-31 too, not just native Android 12+ splash),
  same purple as the Onboarding hero so first-launch has no visual seam.
- **Reconciled a redesign done directly in Antigravity (outside this session)** — a glassmorphic
  `AppHeader`, dark pill `FloatingPillNavBar`, and amber `LoaderWidget` arrived already written.
  Fixed a real compile bug (`tween(durationMs = …)` — the actual parameter is `durationMillis`),
  a real double-bottom-nav bug (`AppRoot` still rendered its own `NavigationBar` around screens
  that had grown their own pill nav), and finished the incomplete wiring (Settings tab was a
  dead no-op; Premium/Settings had no way back to other tabs once the shared nav bar was
  removed). `CreepButton` (an animated novelty button) exists but is intentionally unused —
  never fit this app's identity, left for the user to decide on.
- **Floating nav bar made to actually float** — it had rounded corners from the start, but
  every screen used `Scaffold(bottomBar = …)`, which reserves hard space and clips content
  above it, so there was nothing to see through the transparent corners. Changed to an overlay
  `Box` on all 4 tabs: content scrolls the full height, the pill nav sits on top.
- **Header + nav bar wired to the Appearance setting** — both were hardcoded dark regardless of
  the Light/Dark/System choice in Settings. Now use `MaterialTheme.colorScheme.inverseSurface`
  (the same token Snackbar/Tooltip use for "floating contrast chip, still theme-aware") for the
  bar, and `colorScheme.primary` — the actual brand color, already theme-differentiated — for
  the selected tab chip.
- **Screen transitions** — `NavHost` had no `enterTransition`/`exitTransition` at all (instant
  cuts); added a short fade+slide on every navigation event.

### AdMob — all 5 ad formats + mandatory consent (UMP)

- **Architecture**: every AdMob/UMP SDK call lives in one file, `composeApp/androidMain/data/ads/AdsController.kt`
  — `androidApp` only makes thin, plain-Kotlin calls into it (same "SDK-specific code stays out
  of androidApp" pattern as the download engine). commonMain screens never see AdMob types;
  they call `expect` functions (`showInterstitialAdIfAvailable`, `showRewardedAd`) and `expect`
  composables (`BannerAdSlot`, `NativeAdCard`) exactly like every other platform-specific thing
  in this app.
- **Consent (UMP) is not optional** — showing ads to EEA/UK/California users without asking is a
  legal requirement, not an AdMob-policy nicety. `AdsController.initializeConsentAndAds()` runs
  the full UMP flow and only calls `MobileAds.initialize()` if `canRequestAds()` is actually
  true afterward; a "Manage ad consent" row appears in Settings (only when
  `isPrivacyOptionsRequired()` — hidden for users the form doesn't apply to).
- **Placements**: Banner below the Home header; Native ad card in Home's idle state (styled to
  match the app's card language, built as a plain `NativeAdView` + programmatic child views
  since `composeApp`'s KMP-library target can't reliably use its own `R` class for XML layouts —
  see §11 of the earlier log); Interstitial on "Download Another" (natural break after a
  completed task); Rewarded ad on the Premium screen as a genuine "watch an ad → 24h free
  Premium unlock" — a real reward via a new `PremiumStore.grantTemporaryUnlock`, not a fake
  confirmation; App Open on process-foreground (via `ProcessLifecycleOwner`, correctly
  distinguished from "an Activity in this app resumed because an ad closed" — Google's own
  reference pattern), gated behind onboarding being complete and Google's 4-hour freshness
  window.
- **Test IDs only, everywhere** — every ad unit ID (`AdUnitIds.kt`) and the App ID (manifest
  meta-data) are Google's own published test values. They render real "Test Ad" placeholders
  and are safe to ship as-is for development, but **will never earn revenue** — swap in your own
  AdMob console App ID + one ad unit ID per format before a real release, and not before, since
  requesting live ads during development risks an invalid-traffic flag on the AdMob account.
- **Privacy Policy and the Home "About" dialog rewritten for accuracy** — both previously said
  "no advertising SDK" / "no data sent anywhere except the site you paste a link from," which
  AdMob directly contradicts. Now disclose Google AdMob specifically: what it can collect
  (device/ad identifiers, not download history or pasted links), that consent gates it where
  legally required, and where to change that choice later.
- Manifest permissions `AD_ID` and the Privacy Sandbox `ACCESS_ADSERVICES_*` set are
  **auto-declared by the SDK itself** (25.5.0+) — verified present in the actual merged
  manifest, not manually added.

## 12. Explicitly deferred (not built yet)

- **True pause/resume** — see the limitation above; cancel works today, pause-and-continue doesn't yet.
- **Retry with exponential backoff** for transient failures (network blip vs. permanent error) —
  `Result.failure()` today; wiring `Result.retry()` + a `BackoffPolicy` for the transient cases is
  a natural next step once failure classification exists.
- **Storage management screen** (cache size, clear-completed/clear-all with confirmation) —
  reasonable to build now that WorkManager exists (so "clear all" can check nothing's active first).
- **Download queue / true concurrency** — still one slot, replace-on-new; "max simultaneous
  downloads" still isn't a setting since it'd be non-functional today.
- **Room migration** — history is still a JSON file via `HistoryStore`; fine at this scale.
- **Full accessibility audit** — new icons/rows have content descriptions, but no scalable-text/
  contrast/touch-target pass has been done.
- **No in-app yt-dlp update mechanism** — yt-dlp is bundled and pinned at *build time* via
  Chaquopy's `pip install` in `androidApp/build.gradle.kts`; there is no `BinaryUpdater` or
  runtime-update path (an earlier review draft referenced one — it doesn't exist in this codebase
  and was never built). A site-extraction break requires rebuilding and reinstalling the APK.
- `Contact Support` uses a placeholder `support@example.com` — needs a real address before
  publishing.
- **No on-device testing — and it's a harder blocker than "the emulator needs an on-device tap"**:
  this APK is arm64-v8a only by design (`androidApp/build.gradle.kts` — Chaquopy's Python runtime
  and the bundled ffmpeg binaries are arm64-only), and the only Android system image installed in
  this sandbox is x86_64. There is no ARM emulator available here at all, so even past the
  ADB-authorization wall this sandbox's GUI/headless emulators both hit, the app likely wouldn't
  load its native libraries. Real verification needs an actual phone (or an Apple Silicon Mac's
  native-arm64 emulator) — not fixable from this environment. Everything in this log is verified
  by successful compile + `assembleDebug` (including manifest-merge checks for the trickiest
  pieces, like the foreground-service type above) and careful pattern review — never by running
  the app.
- **AdMob is compiled and manifest-verified only, never seen rendering a real ad** — same
  environment limitation as everything else, but worth calling out specifically here since ad
  SDKs are notorious for looking correct in code review while having a subtle runtime issue
  (wrong ad size, a view never actually attached, a callback that never fires). Test devices
  show Google's placeholder "Test Ad" creative, not your real inventory — install on a real
  device and confirm all 5 formats actually appear before trusting this fully.
- **CreepButton** — the unused novelty button from the Antigravity redesign — still sitting
  unwired; needs a decision (use it somewhere, or remove it) rather than staying dead code
  indefinitely.
- **Mediation is not set up** — see §13 below for why this, not anything in this codebase, is the
  single biggest lever left for real eCPM.

## 13. Revenue-optimization pass

Follow-up to §11, specifically in response to "earn more revenue" — every change here is a real,
policy-safe lever, not just more ad surface for its own sake. **An AdMob account that gets
policy-flagged for excessive/disruptive ads earns zero**, so anything that looked like it would
trade long-term account safety for short-term impressions was deliberately left out (see "What
was deliberately not done" below).

- **Banner switched from fixed `AdSize.BANNER` to an anchored adaptive size**
  (`AdSize.getLargeAnchoredAdaptiveBannerAdSize`, using a dedicated adaptive-banner test unit ID)
  — sized to the actual device width at runtime instead of a fixed 320×50. This is Google's own
  current recommendation over fixed sizes specifically because it fills more of the slot and
  typically clears a meaningfully higher eCPM. Used everywhere `BannerAdSlot` is placed.
- **Native ad now includes a `MediaView`** (rounded, clipped image/video container, landscape
  aspect ratio via `NativeAdOptions`) instead of text+icon+CTA only — native ads with a media
  asset generally clear a higher eCPM, and most fill responses include one. Falls back to hiding
  the media container gracefully when a response has no media content.
- **New ad surfaces** (more impressions, all at natural/unobtrusive spots, not mid-task
  interruptions):
  - Banner added to the Downloads screen (below the top bar) and Settings screen (bottom of the
    scroll, past the About section).
  - Native ad cards now appear periodically inside the Downloads history list — every 6th row,
    both in the date-grouped and size-sorted views — the standard in-feed-native density most
    content apps use; frequent enough to matter, not so frequent it turns the list into an ad wall.
- **Rewarded Interstitial — a new, 6th ad format** (`AdsController.showRewardedInterstitialAd`,
  its own `expect`/`actual`, its own test ad unit ID). This is a genuinely additive inventory
  pool, not a duplicate of plain Rewarded: the Premium screen now offers both side-by-side —
  "Watch ad (+24h)" (plain Rewarded) and "Longer ad (+48h)" (Rewarded Interstitial) — giving users
  a choice increases overall ad-watch conversion. Multiple watches now **stack** (extend from the
  current unlock's expiry instead of resetting it), so a second ad is never wasted — a small
  honest incentive to watch more than one.
- **Frequency capping** (`AdsController.FrequencyGuard`) — Interstitial capped to at most once per
  60s, Rewarded Interstitial once per 90s (independent cap, so the two can't stack on the same
  moment). This is what makes it safe to have *more* full-screen trigger points without risking
  the "excessive/disruptive full-screen ads" AdMob policy violation — the guard, not manual
  discipline, is what enforces the limit.
- **Retry-with-backoff on every preloaded format** (Interstitial, Rewarded, Rewarded Interstitial,
  App Open) — a transient load failure (brief offline blip, momentary no-fill) previously stalled
  that format until some unrelated later trigger happened to call preload again, silently losing
  fill. Now retries automatically at 15s → 30s → 60s → ... capped at 5 minutes, and stops
  retrying if consent/`canShowAds()` ever becomes false mid-backoff.
- **What was deliberately not done, and why**: no ad trigger was added on plain tab navigation
  (Home ↔ Downloads ↔ Settings) — Google's interstitial guidance is explicit that these should
  appear at genuine task-completion/transition points (like the existing "Download another"
  trigger), not on ordinary in-app navigation; doing so risks a policy flag that would cost far
  more revenue than it gains. Banner
  refresh rate was left at the SDK default rather than shortened — Google explicitly discourages
  artificially inflating impressions via aggressive refresh. **Mediation was not implemented** —
  it's genuinely the single biggest real-world eCPM lever (running multiple ad networks in
  competition via AdMob's mediation groups routinely lifts average eCPM 20%+), but it requires
  account-level setup this session has no access to: your own accounts with each additional ad
  network, and configuring mediation groups/waterfalls in the AdMob console itself. Worth doing
  next, from the AdMob console, once you're ready — the code side (adding a mediation adapter
  dependency) is small once the account side exists.

## 14. Downloads / Premium / Settings UI pass

Home had already been through a full visual redesign (glass [AppHeader], [HeroGradient], the
floating pill nav bar); Downloads/Premium/Settings were still on plain default Material3
components (a bare `CenterAlignedTopAppBar`, a flat radio-button list, an uppercase-label list of
cards) — visibly a different, older app. This pass brings all three up to the same visual
language, reusing the same components rather than inventing new ones per screen.

- **Downloads** — `CenterAlignedTopAppBar` replaced with the same [AppHeader] Home uses (title
  "Downloads", tagline live-computed as "`N` saved · `size`"), search now expands as an
  `OutlinedTextField` under the header instead of swapping into the title slot, sort moved from a
  bare icon to a labelled `AssistChip` ("Newest ▾") so the current sort is always visible. Added a
  3-up stat strip (Total / Complete / Saved, computed from the actual record list) above the list.
  Each row is now its own rounded `Surface` card instead of a flat list item, so rows read as
  distinct cards instead of one continuous list. Empty state got the same gradient-circle icon
  treatment as Home's empty state instead of a plain grey `Inbox` icon.
- **Settings** — plain "Settings" text replaced with [AppHeader]; each section (Downloads,
  Behavior, Appearance, About) now has a small icon chip next to its label instead of bare
  uppercase text. The theme picker changed from a vertical `RadioButton` list to a 3-segment
  Light/Dark/System control with icons — glanceable in one look instead of needing to read three
  labels. `NavRow`s (Privacy Policy, Terms, etc.) gained leading icon chips.
- **Premium** — the small icon-circle-and-text hero was replaced with a full-width `HeroGradient`
  banner card (same gradient used elsewhere, plus a soft corner glow like [AppHeader]'s), which
  reads as a proper upsell moment instead of a title. The 3-feature list is now inside its own
  bordered card instead of bare rows. Plan badges ("Best Value" etc.) switched from a flat
  primary-color chip to a small gradient pill matching the hero.
- No behavior changed in this pass — same callbacks, same state, same data; purely visual/layout.
  Verified via `:androidApp:compileDebugKotlin` + `:androidApp:assembleDebug` (both clean); as
  always, never rendered on an actual device from this environment (see §12's device-testing
  limitation) — worth a visual check on a real phone before trusting spacing/contrast fully.

## 15. Banner ads consolidated into one sticky bottom bar + Download Detail gets ads

Banner ads were previously inline in scrolling content on Home/Downloads/Settings — they
scrolled away the moment the user scrolled past them, i.e. lost impressions the moment they
mattered most. Consolidated into one placement pattern used everywhere:

- **`StickyBannerBar`** (new, `ui/components/StickyBannerBar.kt`) — a persistent, non-scrolling
  strip wrapping `BannerAdSlot`, docked to the true bottom of the screen.
- **`AppBottomBarWithAd`** (new, in `FloatingPillNavBar.kt`) — the floating pill nav bar with
  `StickyBannerBar` docked directly beneath it. This is now what Home/Downloads/Premium/Settings
  all call instead of bare `AppBottomNavBar` — the pill still floats over scrolling content
  exactly as before (see the existing Box-overlay-not-Scaffold comment on each screen), the
  banner is the new always-visible element beneath it. Content's bottom clearance spacer bumped
  from 90dp → 170dp on all four screens to keep real content clear of the taller combined bar.
- **Premium** gained a banner ad it never had before (it uses `AppBottomBarWithAd` like the other
  three tabs now).
- **Download Detail** (`DownloadDetailScreen.kt`) — the one real content screen with zero ads
  before this pass — now shows `StickyBannerBar` as an actual `Scaffold(bottomBar = ...)`, which
  is simpler than the tab screens' Box-overlay pattern since this screen has no floating nav pill
  to coexist with; `Scaffold` handles the content-padding math on its own.
- **Deliberately left alone**: Onboarding, Privacy Policy, and Terms of Service still show no ads
  at all. Onboarding is a first-impression/pre-signup flow — Google's own interstitial guidance
  specifically warns against ads at launch, and the same logic applies to any ad on the very
  first screens a new user sees. Privacy Policy/Terms are mandatory legal-disclosure pages users
  are often forced to open before they trust the app; putting ads on them reads as exploiting a
  captive audience and risks both user trust and AdMob's "low-value content" policy. Native ad
  placements (Home idle state, Downloads list) are unchanged — this pass only touched banners.

## 16. Key architecture notes for picking this back up

- `composeApp`/`commonMain` screens take only callback lambdas — never a `NavController` — so
  they stay portable. Navigation itself lives in `androidApp/nav/AppRoot.kt`.
- Local persistence follows one repeated pattern: a commonMain interface in
  `domain/repository/*Store.kt` + an androidMain SharedPreferences implementation in
  `data/repository/SharedPrefs*Store.kt`, wired once in `AppModule` from `MediaSaverApp.onCreate()`.
  (`OnboardingStore`, `PremiumStore`, `SettingsStore` all follow this.)
- Platform-specific actions (file share/open/rename/delete, clipboard read, Wi-Fi check, app-open)
  are `expect`/`actual` in `data/platform/PlatformUtils.kt` / `.android.kt`.
