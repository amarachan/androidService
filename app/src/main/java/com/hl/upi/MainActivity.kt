package com.hl.upi

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hl.upi.data.SettingsManager
import com.hl.upi.service.CallRecordService
import com.hl.upi.ui.theme.UpiTheme
import com.hl.upi.ui.*
import com.hl.upi.util.MaskManager

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager

    // Permission launcher to handle all the necessary permissions at once
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        // If user says yes to all, and service is enabled in settings, we start it
        if (allGranted && settingsManager.isServiceEnabled) {
            startService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)
        
        // If we came here from a decoy, we should disable the decoy icon now
        if (settingsManager.activeMask != null) {
            MaskManager(this).disableDecoy()
        }

        enableEdgeToEdge()
        
        // Asking for permissions as soon as app opens
        requestPermissions()

        setContent {
            UpiTheme {
                // Handling the main navigation part
                MainNavigation(settingsManager)
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
        )
        // Notification permission is needed from Android 13 onwards
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startService() {
        // Starting the background call monitor service
        val intent = Intent(this, CallRecordService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // Android 8+ needs foreground service call
        } else {
            startService(intent)
        }
    }
}

@Composable
fun MainNavigation(settingsManager: SettingsManager) {
    val navController = rememberNavController()
    // Checking if we need to show the PIN lock screen or not
    var isUnlocked by remember { mutableStateOf(settingsManager.appPin == null) }

    if (!isUnlocked) {
        // Show lock screen if PIN is set in settings
        LockScreen(
            correctPin = settingsManager.appPin!!,
            onUnlocked = { isUnlocked = true }
        )
    } else {
        // Main flow starts here after unlocking
        NavHost(navController = navController, startDestination = "main") {
            composable("main") { 
                MainScreen(navController, settingsManager) 
            }
            composable("settings") { 
                SettingsScreen(navController, settingsManager) 
            }
            composable("recordings") { 
                RecordingsListScreen(navController) 
            }
        }
    }
}
