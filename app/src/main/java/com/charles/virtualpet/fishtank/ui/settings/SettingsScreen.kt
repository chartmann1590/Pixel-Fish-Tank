package com.charles.virtualpet.fishtank.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.virtualpet.fishtank.audio.BackgroundMusicManager
import com.charles.virtualpet.fishtank.audio.SfxManager
import com.charles.virtualpet.fishtank.backup.BackupRepository
import com.charles.virtualpet.fishtank.backup.BackupValidator
import com.charles.virtualpet.fishtank.data.GameStateRepository
import com.charles.virtualpet.fishtank.domain.GameViewModel
import com.charles.virtualpet.fishtank.ui.theme.PastelBlue
import com.charles.virtualpet.fishtank.ui.theme.PastelGreen
import com.charles.virtualpet.fishtank.ui.theme.PastelPink
import com.charles.virtualpet.fishtank.ui.theme.PastelPurple
import com.charles.virtualpet.fishtank.ui.theme.PastelYellow
import com.charles.virtualpet.fishtank.notifications.NotificationChannels
import com.charles.virtualpet.fishtank.notifications.NotificationBuilder
import com.charles.virtualpet.fishtank.notifications.NotificationIds
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.charles.virtualpet.fishtank.ui.components.AdMobBanner

@Composable
private fun TimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, initialHour)
        calendar.set(Calendar.MINUTE, initialMinute)
        
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeSelected(hour, minute)
            },
            initialHour,
            initialMinute,
            false // 24-hour format
        )
        timePickerDialog.setOnDismissListener { onDismiss() }
        timePickerDialog.show()
    }
}

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    repository: GameStateRepository,
    sfxManager: SfxManager?,
    bgMusicManager: BackgroundMusicManager?,
    onBack: () -> Unit,
    onReplayTutorial: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settings = gameState.settings
    val context = LocalContext.current
    val backupRepository = remember { BackupRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Sync sfxEnabled setting with SfxManager
    LaunchedEffect(settings.sfxEnabled) {
        sfxManager?.setEnabled(settings.sfxEnabled)
    }
    
    // Sync bgMusicEnabled setting with BackgroundMusicManager
    LaunchedEffect(settings.bgMusicEnabled) {
        bgMusicManager?.setEnabled(settings.bgMusicEnabled)
    }
    
    // Permission launcher for notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable notifications
            viewModel.updateNotificationSettings(true, settings.reminderTimes)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications enabled")
            }
        } else {
            // Permission denied, keep notifications disabled
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notification permission is required to enable notifications")
            }
        }
    }

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 58.dp) // Space for banner (50dp) + padding
    ) {
        // Beautiful Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelBlue, PastelGreen, PastelBlue)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⚙️",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Customize your fish tank experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Notifications Card with gradient
            NotificationCard(
                settings = settings,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onNotificationToggle = { enabled ->
                    if (enabled) {
                        // User wants to enable notifications
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Android 13+ requires runtime permission
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                // Permission already granted
                                viewModel.updateNotificationSettings(true, settings.reminderTimes)
                            } else {
                                // Request permission
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            // Android 12 and below - permission not required
                            viewModel.updateNotificationSettings(true, settings.reminderTimes)
                        }
                    } else {
                        // User wants to disable notifications
                        viewModel.updateNotificationSettings(false, settings.reminderTimes)
                    }
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Sound Effects Card with gradient
            SoundEffectsCard(
                settings = settings,
                onSfxToggle = { enabled ->
                    viewModel.updateSfxSettings(enabled)
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Background Music Card with gradient
            BackgroundMusicCard(
                settings = settings,
                onBgMusicToggle = { enabled ->
                    viewModel.updateBgMusicSettings(enabled)
                },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Tutorial Card
            TutorialCard(
                onReplayTutorial = onReplayTutorial,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Backup & Restore Card
            BackupRestoreSection(
                gameState = gameState,
                backupRepository = backupRepository,
                repository = repository,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // About Card with gradient
            AboutCard(
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Back Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "← Back to Tank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Snackbar for backup/restore feedback
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun NotificationCard(
    settings: com.charles.virtualpet.fishtank.domain.model.Settings,
    viewModel: GameViewModel,
    onNotificationToggle: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Parse time string to hour and minute
    fun parseTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(19, 0) // Default to 7:00 PM
        }
    }
    
    // Format hour and minute to time string
    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    // Time picker for daily reminder
    var showDailyReminderTimePicker by remember { mutableStateOf(false) }
    val (dailyHour, dailyMinute) = parseTime(settings.dailyReminderTime)
    
    if (showDailyReminderTimePicker) {
        TimePickerDialog(
            context = context,
            initialHour = dailyHour,
            initialMinute = dailyMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updateDailyReminderSettings(
                    enabled = settings.dailyReminderEnabled,
                    time = formatTime(hour, minute)
                )
                showDailyReminderTimePicker = false
            },
            onDismiss = { showDailyReminderTimePicker = false }
        )
    }
    
    // Time picker for quiet hours start
    var showQuietHoursStartPicker by remember { mutableStateOf(false) }
    val (quietStartHour, quietStartMinute) = parseTime(settings.quietHoursStart)
    
    if (showQuietHoursStartPicker) {
        TimePickerDialog(
            context = context,
            initialHour = quietStartHour,
            initialMinute = quietStartMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updateQuietHoursSettings(
                    enabled = settings.quietHoursEnabled,
                    start = formatTime(hour, minute),
                    end = settings.quietHoursEnd
                )
                showQuietHoursStartPicker = false
            },
            onDismiss = { showQuietHoursStartPicker = false }
        )
    }
    
    // Time picker for quiet hours end
    var showQuietHoursEndPicker by remember { mutableStateOf(false) }
    val (quietEndHour, quietEndMinute) = parseTime(settings.quietHoursEnd)
    
    if (showQuietHoursEndPicker) {
        TimePickerDialog(
            context = context,
            initialHour = quietEndHour,
            initialMinute = quietEndMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updateQuietHoursSettings(
                    enabled = settings.quietHoursEnabled,
                    start = settings.quietHoursStart,
                    end = formatTime(hour, minute)
                )
                showQuietHoursEndPicker = false
            },
            onDismiss = { showQuietHoursEndPicker = false }
        )
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPink, PastelPurple)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔔",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Get reminders to check on your fish",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = onNotificationToggle,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                if (settings.notificationsEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📱 Notifications will remind you to feed and care for your fish daily.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                    
                    
                    // Sub-settings (only shown if master toggle is on and permission granted)
                    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    } else {
                        true // Android 12 and below
                    }
                    
                    if (hasPermission) {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Daily Check-in Reminder
                        NotificationSubSettingRow(
                            title = "Daily Check-in Reminder",
                            description = "Get a daily reminder to check on your fish",
                            enabled = settings.dailyReminderEnabled,
                            onToggle = { enabled ->
                                viewModel.updateDailyReminderSettings(
                                    enabled = enabled,
                                    time = settings.dailyReminderTime
                                )
                            }
                        )
                        
                        if (settings.dailyReminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDailyReminderTimePicker = true }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Reminder time: ${settings.dailyReminderTime}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Tap to change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Status Nudges
                        NotificationSubSettingRow(
                            title = "Status Nudges",
                            description = "Get notified when your fish needs attention (hungry/dirty/sad)",
                            enabled = settings.statusNudgesEnabled,
                            onToggle = { enabled ->
                                viewModel.updateStatusNudgesSettings(enabled)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Persistent Notification
                        NotificationSubSettingRow(
                            title = "Persistent Fish Status",
                            description = "Show ongoing notification with fish status",
                            enabled = settings.persistentNotificationEnabled,
                            onToggle = { enabled ->
                                viewModel.updatePersistentNotificationSettings(enabled)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Quiet Hours
                        NotificationSubSettingRow(
                            title = "Quiet Hours",
                            description = "Don't send notifications during these hours",
                            enabled = settings.quietHoursEnabled,
                            onToggle = { enabled ->
                                viewModel.updateQuietHoursSettings(
                                    enabled = enabled,
                                    start = settings.quietHoursStart,
                                    end = settings.quietHoursEnd
                                )
                            }
                        )
                        
                        if (settings.quietHoursEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showQuietHoursStartPicker = true }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Start: ${settings.quietHoursStart}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Tap to change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showQuietHoursEndPicker = true }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "End: ${settings.quietHoursEnd}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Tap to change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Debug test button (only in debug builds)
                        // Note: BuildConfig is generated at compile time, so we'll always show this in debug
                        // In production builds, this code path won't be included due to dead code elimination
                        val isDebugBuild = try {
                            @Suppress("UNCHECKED_CAST")
                            Class.forName("com.charles.virtualpet.fishtank.BuildConfig")
                                .getDeclaredField("DEBUG")
                                .get(null) as Boolean
                        } catch (e: Exception) {
                            false
                        }
                        if (isDebugBuild) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    // Initialize channels if not already done
                                    NotificationChannels.createChannels(context)
                                    
                                    // Check permission
                                    val notificationManager = NotificationManagerCompat.from(context)
                                    if (notificationManager.areNotificationsEnabled()) {
                                        val builder = NotificationBuilder(context)
                                        val notification = builder.buildTestNotification()
                                        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        manager.notify(NotificationIds.DAILY_REMINDER, notification)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Test notification sent!")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Notifications not enabled")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "🧪 Test Notification (Debug)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSubSettingRow(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun SoundEffectsCard(
    settings: com.charles.virtualpet.fishtank.domain.model.Settings,
    onSfxToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPink, PastelPurple)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔊",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "Sound Effects",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Enable game sound effects",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Switch(
                        checked = settings.sfxEnabled,
                        onCheckedChange = onSfxToggle,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                if (settings.sfxEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🎵 Sound effects will play during gameplay for a more immersive experience.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundMusicCard(
    settings: com.charles.virtualpet.fishtank.domain.model.Settings,
    onBgMusicToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelBlue, PastelGreen)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🎵",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "Background Music",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Soft ambient music while playing",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Switch(
                        checked = settings.bgMusicEnabled,
                        onCheckedChange = onBgMusicToggle,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                if (settings.bgMusicEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🎶 Gentle background music will play softly while you care for your fish.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialCard(
    onReplayTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPurple, PastelPink)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "📚",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Column {
                        Text(
                            text = "Tutorial",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Go through the tutorial again",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onReplayTutorial,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Replay Tutorial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelYellow, PastelGreen)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "ℹ️",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Pixel Fish Tank v1.0\n\nTake care of your virtual pet fish by feeding, cleaning, playing mini-games, and decorating your tank!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Links section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌐 Website",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "https://pixel-fish-tank.web.app/",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pixel-fish-tank.web.app/"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "💻 GitHub",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "https://github.com/chartmann1590/Pixel-Fish-Tank",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chartmann1590/Pixel-Fish-Tank"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupRestoreSection(
    gameState: com.charles.virtualpet.fishtank.domain.model.GameState,
    backupRepository: BackupRepository,
    repository: GameStateRepository,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lastBackupTime by repository.lastBackupTime.collectAsStateWithLifecycle(initialValue = null)
    
    var showImportConfirmation by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val envelope = backupRepository.exportCurrentState(gameState, repository)
                    val result = backupRepository.writeBackupToUri(uri, envelope, repository)
                    result.fold(
                        onSuccess = {
                            snackbarHostState.showSnackbar("Backup saved successfully")
                        },
                        onFailure = { exception ->
                            snackbarHostState.showSnackbar("Failed to save backup: ${exception.message}")
                        }
                    )
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error creating backup: ${e.message}")
                }
            }
        }
    }
    
    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val result = backupRepository.readBackupFromUri(uri)
                    result.fold(
                        onSuccess = { envelope ->
                            val validationResult = BackupValidator.validate(envelope)
                            when (validationResult) {
                                is com.charles.virtualpet.fishtank.backup.ValidationResult.Success -> {
                                    pendingImportUri = uri
                                    showImportConfirmation = true
                                }
                                is com.charles.virtualpet.fishtank.backup.ValidationResult.Error -> {
                                    snackbarHostState.showSnackbar("Invalid backup: ${validationResult.message}")
                                }
                            }
                        },
                        onFailure = { exception ->
                            snackbarHostState.showSnackbar("Failed to read backup: ${exception.message}")
                        }
                    )
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error reading backup: ${e.message}")
                }
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "💾",
                    style = MaterialTheme.typography.displaySmall
                )
                Column {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Export or import your game data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "⚠️ Import will overwrite your current progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Last backup time
            val lastBackupText = lastBackupTime?.let { epoch ->
                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                "Last backup: ${dateFormat.format(Date(epoch))}"
            } ?: "Never backed up"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = lastBackupText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.getDefault()).format(Date())
                    val filename = "pixelfishtank-backup-$timestamp.json"
                    exportLauncher.launch(filename)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PastelBlue
                )
            ) {
                Text(
                    text = "📤 Export Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Button(
                onClick = {
                    importLauncher.launch(arrayOf("application/json", "text/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PastelGreen
                )
            ) {
                Text(
                    text = "📥 Import Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        // AdMob Banner
        AdMobBanner()
    }
    
    // Confirmation dialog
    if (showImportConfirmation && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmation = false
                pendingImportUri = null
            },
            title = {
                Text("Confirm Import")
            },
            text = {
                Text("This will replace your current save. Continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri
                        if (uri != null) {
                            coroutineScope.launch {
                                try {
                                    val readResult = backupRepository.readBackupFromUri(uri)
                                    readResult.fold(
                                        onSuccess = { envelope ->
                                            val importResult = backupRepository.importState(envelope, repository)
                                            importResult.fold(
                                                onSuccess = {
                                                    snackbarHostState.showSnackbar("Backup restored successfully")
                                                    showImportConfirmation = false
                                                    pendingImportUri = null
                                                },
                                                onFailure = { exception ->
                                                    snackbarHostState.showSnackbar("Failed to restore backup: ${exception.message}")
                                                    showImportConfirmation = false
                                                    pendingImportUri = null
                                                }
                                            )
                                        },
                                        onFailure = { exception ->
                                            snackbarHostState.showSnackbar("Failed to read backup: ${exception.message}")
                                            showImportConfirmation = false
                                            pendingImportUri = null
                                        }
                                    )
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error restoring backup: ${e.message}")
                                    showImportConfirmation = false
                                    pendingImportUri = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        pendingImportUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

