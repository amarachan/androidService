package com.hl.upi.ui

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hl.upi.data.SettingsManager
import com.hl.upi.service.CallRecordService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, settingsManager: SettingsManager) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(settingsManager.isServiceEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Monitor") },
                actions = {
                    IconButton(onClick = { navController.navigate("recordings") }) {
                        Icon(Icons.Default.List, contentDescription = "Recordings")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isEnabled) "Service is Running" else "Service is Stopped",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = {
                    isEnabled = it
                    settingsManager.isServiceEnabled = it
                    val intent = Intent(context, CallRecordService::class.java)
                    if (it) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    } else {
                        context.stopService(intent)
                    }
                }
            )
        }
    }
}
