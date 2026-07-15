package com.charles.virtualpet.fishtank.data.feedback

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DiagnosticsHelper {

    fun collectDiagnostics(context: Context): String {
        val appInfo = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
            Pair(appName, packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            Pair("Unknown", null)
        }

        val storageInfo = getStorageInfo()
        val memoryInfo = getMemoryInfo(context)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date())

        return buildString {
            appendLine("## Diagnostics")
            appendLine()
            appendLine("- App: ${appInfo.first}")
            appendLine("- Package: ${context.packageName}")
            val packageInfo = appInfo.second
            if (packageInfo != null) {
                appendLine("- Version: ${packageInfo.versionName} (${packageInfo.longVersionCode})")
            }
            appendLine("- Device: ${Build.MODEL}")
            appendLine("- Manufacturer: ${Build.MANUFACTURER}")
            appendLine("- Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
            appendLine("- Locale: ${Locale.getDefault()}")
            appendLine("- Time Zone: ${TimeZone.getDefault().id}")
            appendLine("- Storage Free/Total: ${storageInfo.first} / ${storageInfo.second}")
            appendLine("- Memory Free/Total: ${memoryInfo.first} / ${memoryInfo.second}")
            appendLine("- Timestamp: $timestamp")
        }
    }

    private fun getStorageInfo(): Pair<String, String> {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong
            Pair(formatBytes(availableBlocks * blockSize), formatBytes(totalBlocks * blockSize))
        } catch (e: Exception) {
            Pair("Unknown", "Unknown")
        }
    }

    private fun getMemoryInfo(context: Context): Pair<String, String> {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalMem = memInfo.totalMem
            val availMem = memInfo.availMem
            Pair(formatBytes(availMem), formatBytes(totalMem))
        } catch (e: Exception) {
            Pair("Unknown", "Unknown")
        }
    }

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "$bytes B"
        } else {
            String.format(Locale.US, "%.1f %s", value, units[unitIndex])
        }
    }
}
