package com.charles.virtualpet.fishtank

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.android.gms.ads.MobileAds
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.data.ImageCacheManager
import com.charles.virtualpet.fishtank.data.FirebaseStoreRepository
import com.charles.virtualpet.fishtank.data.workers.StoreSyncWorker
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.domain.GameViewModelFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.charles.virtualpet.fishtank.audio.BackgroundMusicManager
import com.charles.virtualpet.fishtank.audio.SfxManager
import com.charles.virtualpet.fishtank.ui.components.InterstitialAdManager
import com.charles.virtualpet.fishtank.ui.navigation.NavGraph
import com.charles.virtualpet.fishtank.ui.theme.PixelFishTankTheme
import com.charles.virtualpet.fishtank.notifications.NotificationChannels
import com.charles.virtualpet.fishtank.notifications.NotificationPrefs
import com.charles.virtualpet.fishtank.notifications.PersistentNotificationManager
import com.charles.virtualpet.fishtank.share.ScreenshotFileStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var sfxManager: SfxManager? = null
    private var bgMusicManager: BackgroundMusicManager? = null
    private val notificationPrefs: NotificationPrefs by lazy { NotificationPrefs(applicationContext) }
    private val persistentNotificationManager = PersistentNotificationManager(this)
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Store repository
    private val imageCacheManager by lazy { ImageCacheManager(this) }
    private val storeRepository by lazy { FirebaseStoreRepository(this, imageCacheManager) }
    
    // Interstitial ad manager
    private val interstitialAdManager by lazy {
        InterstitialAdManager(
            context = this
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle notification action intents
        handleNotificationAction(intent)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
        
        // Initialize Analytics
        com.charles.virtualpet.fishtank.analytics.AnalyticsHelper.initialize(this)
        com.charles.virtualpet.fishtank.analytics.AnalyticsHelper.logAppOpen()
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
        
        // Preload interstitial ad
        interstitialAdManager.loadAd()
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        // Hide system bars for fullscreen immersive mode
        hideSystemBars()
        
        // Initialize SfxManager
        sfxManager = SfxManager(this)
        
        // Initialize BackgroundMusicManager
        bgMusicManager = BackgroundMusicManager(this)
        
        // Initialize notification channels
        NotificationChannels.createChannels(this)
        
        // Clean up old screenshots on app start
        ScreenshotFileStore.cleanupOldScreenshots(this)
        
        // Register repository with DecorationStore for lookup
        com.charles.virtualpet.fishtank.data.DecorationStore.setRepository(storeRepository)
        
        // Schedule store sync worker (15 minutes)
        scheduleStoreSyncWorker()
        
        // Trigger immediate sync on app open
        coroutineScope.launch {
            storeRepository.syncStoreItems()
        }
        
        setContent {
            PixelFishTankTheme {
                val viewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(this@MainActivity)
                )
                val repository = GameStateRepository(this@MainActivity)
                val navController = rememberNavController()
                
                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    repository = repository,
                    storeRepository = storeRepository,
                    sfxManager = sfxManager,
                    bgMusicManager = bgMusicManager,
                    interstitialAdManager = interstitialAdManager
                )
            }
        }
    }
    
    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Keep screen on during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }
    
    override fun onPause() {
        super.onPause()
        bgMusicManager?.pause()
        com.charles.virtualpet.fishtank.analytics.AnalyticsHelper.logAppBackground()
    }
    
    override fun onResume() {
        super.onResume()
        bgMusicManager?.resume()
        // Track app open time for notification decision engine
        coroutineScope.launch {
            notificationPrefs.updateLastAppOpenEpoch(System.currentTimeMillis())
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sfxManager?.release()
        sfxManager = null
        bgMusicManager?.release()
        bgMusicManager = null
    }
    
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationAction(intent)
    }
    
    private fun handleNotificationAction(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action
        // Actions open the app - user can manually feed/clean from the tank screen
        // Future enhancement: could auto-trigger feed/clean actions here
    }
    
    private fun scheduleStoreSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<StoreSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "store_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

