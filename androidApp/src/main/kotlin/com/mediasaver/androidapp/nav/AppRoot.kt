package com.mediasaver.androidapp.nav

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mediasaver.app.data.ads.AdsController
import com.mediasaver.app.domain.model.ThemeMode
import com.mediasaver.app.ui.components.BentoDrawerContent
import com.mediasaver.app.ui.screens.DownloadDetailScreen
import com.mediasaver.app.ui.screens.DownloadsScreen
import com.mediasaver.app.ui.screens.HomeScreen
import com.mediasaver.app.ui.screens.OnboardingScreen
import com.mediasaver.app.ui.screens.PremiumScreen
import com.mediasaver.app.ui.screens.PrivacyPolicyScreen
import com.mediasaver.app.ui.screens.SettingsScreen
import com.mediasaver.app.ui.screens.TermsOfServiceScreen
import com.mediasaver.app.ui.state.AppViewModel
import com.mediasaver.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DOWNLOADS = "downloads"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
    const val DETAIL = "detail/{recordId}"
    fun detail(recordId: String) = "detail/$recordId"
}

/** Switches tabs without stacking duplicate destinations — the standard bottom-nav pattern. */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * App-wide root: owns the [AppViewModel] instance and the real navigation graph. The 4 persistent
 * tabs (Home/Downloads/Premium/Settings) each render their own [com.mediasaver.app.ui.components.AppBottomNavBar]
 * as part of their own `Scaffold` — there's deliberately no second, shared bottom bar wrapping
 * the `NavHost` here, since that would double up with each screen's own. Privacy/Terms/Detail are
 * full-screen pushes with a plain back arrow instead, reached from within a tab rather than being
 * tabs themselves.
 *
 * Lives in `androidApp` (not `composeApp`'s commonMain) because it depends on the classic,
 * stable `androidx.navigation:navigation-compose` artifact — the Compose Multiplatform build of
 * Navigation is still alpha, and this app is Android-only in practice (see README), so the
 * mature Android-only library is the safer, "professional" choice here. The screens themselves
 * stay in commonMain and navigation-agnostic — they only take callback lambdas, never a
 * [NavHostController] — so they remain reusable if a desktop/iOS target is ever added.
 *
 * @param activity     The hosting Activity — needed by the UMP consent flow and every full-screen
 *                      ad format's `.show(activity)` call, which `expect`/`actual` functions in
 *                      commonMain can't carry themselves (see [AdsController]).
 * @param sharedUrl    A URL shared into the app from another app's Share sheet, auto-submitted
 *                      every time it changes (cold start via Intent, or re-share while already
 *                      running via onNewIntent — see MainActivity).
 * @param resumeSignal Bumped by MainActivity.onResume() — passed through to HomeScreen for its
 *                      one-shot clipboard check.
 */
@Composable
fun AppRoot(
    activity: Activity,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    sharedUrl: String?,
    resumeSignal: Int
) {
    val viewModel = remember { AppViewModel() }
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }

    val settings by viewModel.settings.collectAsState()
    val effectiveDarkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> darkTheme
    }

    AppTheme(darkTheme = effectiveDarkTheme, dynamicColor = dynamicColor) {
        var startDestination by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            startDestination = if (viewModel.hasCompletedOnboarding()) Routes.HOME else Routes.ONBOARDING
        }

        val resolvedStart = startDestination
        if (resolvedStart == null) {
            // Brief gate while the SharedPreferences-backed onboarding check resolves.
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            return@AppTheme
        }

        // Notifications need an explicit runtime grant on API 33+ (without it, DownloadWorker's
        // progress/complete/failed notifications are silently dropped — the download itself still
        // works, the user just won't see it). Asked once per cold start, not tied to the first
        // download, so it's granted well before it's actually needed.
        val context = LocalContext.current
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* Either outcome is fine — downloads work regardless, just without a visible notification if denied. */ }
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Consent (UMP) first, ads SDK init only if that consent flow actually allows requesting
        // ads (see AdsController.initializeConsentAndAds) — then preload every format, and only
        // now let the App Open ad start showing on future resumes, so it can never interrupt
        // first-run onboarding or this very first consent prompt.
        LaunchedEffect(Unit) {
            AdsController.initializeConsentAndAds(activity) {
                AdsController.preloadAll(activity.applicationContext)
                AdsController.enableAppOpenAds()
            }
        }

        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

        val history by viewModel.history.collectAsState()
        val activePlan by viewModel.activePlan.collectAsState()
        val temporaryUnlockExpiresAt by viewModel.temporaryUnlockExpiresAt.collectAsState()
        val isPremiumActive = activePlan != null || temporaryUnlockExpiresAt != null
        val networkConnection by viewModel.networkConnection.collectAsState()

        // Re-fires on every distinct shared URL, not just once — covers both cold start and a
        // second share arriving via onNewIntent while the app is already running.
        LaunchedEffect(sharedUrl) {
            if (!sharedUrl.isNullOrBlank()) {
                viewModel.onUrlChanged(sharedUrl)
                viewModel.onUrlSubmit(sharedUrl)
                navController.navigateTopLevel(Routes.HOME)
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentRoute != Routes.ONBOARDING,
            drawerContent = {
                BentoDrawerContent(
                    currentRoute = currentRoute,
                    downloadCount = history.size,
                    isPremiumActive = isPremiumActive,
                    onNavigate = { targetRoute ->
                        coroutineScope.launch { drawerState.close() }
                        when (targetRoute) {
                            Routes.HOME -> navController.navigateTopLevel(Routes.HOME)
                            Routes.DOWNLOADS -> navController.navigateTopLevel(Routes.DOWNLOADS)
                            Routes.PREMIUM -> navController.navigateTopLevel(Routes.PREMIUM)
                            Routes.SETTINGS -> navController.navigateTopLevel(Routes.SETTINGS)
                            else -> navController.navigate(targetRoute)
                        }
                    },
                    onOpenPrivacy = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.PRIVACY)
                    },
                    onOpenTerms = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.TERMS)
                    },
                    onClose = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            // A subtle fade + slide on every navigation event — tab switches and push/pop alike —
            // instead of the default instant cut. Kept short (200ms) and low-amplitude so it reads
            // as polish, not something the user has to wait through.
            NavHost(
                navController      = navController,
                startDestination   = resolvedStart,
                enterTransition    = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 10 } },
                exitTransition     = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition  = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 10 } }
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onFinished = {
                            viewModel.markOnboardingCompleted()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel           = viewModel,
                        onOpenDownloads     = { navController.navigateTopLevel(Routes.DOWNLOADS) },
                        onOpenPremium       = { navController.navigateTopLevel(Routes.PREMIUM) },
                        onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY) },
                        onOpenSettings      = { navController.navigateTopLevel(Routes.SETTINGS) },
                        onOpenDrawer        = { coroutineScope.launch { drawerState.open() } },
                        resumeSignal        = resumeSignal
                    )
                }
                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        records             = history,
                        confirmBeforeDelete = settings.confirmBeforeDelete,
                        onBack              = null,
                        onOpenDrawer        = { coroutineScope.launch { drawerState.open() } },
                        onOpenPremium       = { navController.navigateTopLevel(Routes.PREMIUM) },
                        isPremiumActive     = isPremiumActive,
                        networkConnection   = networkConnection,
                        onRetryConnection   = viewModel::refreshNetworkStatus,
                        onOpenDetail        = { record -> navController.navigate(Routes.detail(record.id)) },
                        onDeleteConfirmed   = { record -> viewModel.deleteRecord(record) },
                        onStartNewDownload  = { navController.navigateTopLevel(Routes.HOME) },
                        onClearAll          = viewModel::clearHistory
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        settings                    = settings,
                        onThemeModeChange           = viewModel::setThemeMode,
                        onWifiOnlyChange            = viewModel::setWifiOnlyDownloads,
                        onWarnOnCellularChange      = viewModel::setWarnOnCellular,
                        onAskBeforeDownloadChange   = viewModel::setAskBeforeDownload,
                        onConfirmBeforeDeleteChange = viewModel::setConfirmBeforeDelete,
                        onAutoDetectClipboardChange = viewModel::setAutoDetectClipboard,
                        onAdsEnabledChange          = viewModel::setAdsEnabled,
                        onOpenPrivacyPolicy         = { navController.navigate(Routes.PRIVACY) },
                        onOpenTerms                 = { navController.navigate(Routes.TERMS) },
                        onOpenHome                  = null,
                        onOpenDownloads             = { navController.navigateTopLevel(Routes.DOWNLOADS) },
                        onOpenDrawer                = { coroutineScope.launch { drawerState.open() } },
                        onOpenPremium               = { navController.navigateTopLevel(Routes.PREMIUM) },
                        isPremiumActive             = isPremiumActive,
                        networkConnection           = networkConnection,
                        onOpenAdPrivacyOptions      = if (AdsController.isPrivacyOptionsRequired()) {
                            { AdsController.openPrivacyOptionsForm(activity) {} }
                        } else null
                    )
                }
                composable(Routes.PREMIUM) {
                    PremiumScreen(
                        activePlan               = activePlan,
                        temporaryUnlockExpiresAt = temporaryUnlockExpiresAt,
                        onBack                   = null,
                        onOpenDrawer             = { coroutineScope.launch { drawerState.open() } },
                        onSelectPlan             = viewModel::selectPlan,
                        onWatchRewardedAd        = viewModel::watchRewardedAdForPremium,
                        onWatchRewardedInterstitialAd = viewModel::watchRewardedInterstitialForPremium
                    )
                }
                composable(Routes.PRIVACY) {
                    PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.TERMS) {
                    TermsOfServiceScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route     = Routes.DETAIL,
                    arguments = listOf(navArgument("recordId") { type = NavType.StringType })
                ) { entry ->
                    val recordId = entry.arguments?.getString("recordId")
                    val record = history.firstOrNull { it.id == recordId }
                    if (record != null) {
                        DownloadDetailScreen(
                            record              = record,
                            confirmBeforeDelete = settings.confirmBeforeDelete,
                            onBack              = { navController.popBackStack() },
                            onDelete            = { r -> viewModel.deleteRecord(r) },
                            onRename            = { newTitle, onResult -> viewModel.renameRecord(record.id, newTitle, onResult) }
                        )
                    } else {
                        // Record was deleted (or app restarted mid-navigation) — just back out.
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}
