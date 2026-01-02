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
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.domain.GameViewModelFactory
import com.charles.virtualpet.fishtank.audio.BackgroundMusicManager
import com.charles.virtualpet.fishtank.audio.SfxManager
import com.charles.virtualpet.fishtank.ui.navigation.NavGraph
import com.charles.virtualpet.fishtank.ui.theme.PixelFishTankTheme
import com.charles.virtualpet.fishtank.notifications.NotificationChannels
import com.charles.virtualpet.fishtank.notifications.NotificationPrefs
import com.charles.virtualpet.fishtank.notifications.PersistentNotificationManager
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle notification action intents
        handleNotificationAction(intent)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
        
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
                    sfxManager = sfxManager,
                    bgMusicManager = bgMusicManager
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
}

