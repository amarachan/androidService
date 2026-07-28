package com.hl.upi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hl.upi.data.SettingsManager
import com.hl.upi.util.MaskManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsManager: SettingsManager) {
    val context = LocalContext.current
    val maskManager = remember { MaskManager(context) }
    
    var email by remember { mutableStateOf(settingsManager.targetEmail ?: "") }
    var smtpHost by remember { mutableStateOf(settingsManager.smtpHost) }
    var smtpPort by remember { mutableStateOf(settingsManager.smtpPort) }
    var smtpUser by remember { mutableStateOf(settingsManager.smtpUser ?: "") }
    var smtpPass by remember { mutableStateOf(settingsManager.smtpPass ?: "") }
    var pin by remember { mutableStateOf(settingsManager.appPin ?: "") }
    var activeMask by remember { mutableStateOf(settingsManager.activeMask) }
    
    var useHyvorRelay by remember { mutableStateOf(settingsManager.useHyvorRelay) }
    var hyvorApiKey by remember { mutableStateOf(settingsManager.hyvorApiKey ?: "") }
    var hyvorEndpoint by remember { mutableStateOf(settingsManager.hyvorEndpoint) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Text("General Settings", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; settingsManager.targetEmail = it },
                label = { Text("Target Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it; settingsManager.appPin = it.ifBlank { null } },
                label = { Text("App PIN (leave blank for none)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Delivery Method", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Standard SMTP")
                RadioButton(selected = !useHyvorRelay, onClick = { useHyvorRelay = false; settingsManager.useHyvorRelay = false })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hyvor Relay API")
                RadioButton(selected = useHyvorRelay, onClick = { useHyvorRelay = true; settingsManager.useHyvorRelay = true })
            }

            if (!useHyvorRelay) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("SMTP Configuration", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = smtpHost,
                    onValueChange = { smtpHost = it; settingsManager.smtpHost = it },
                    label = { Text("SMTP Host") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpPort,
                    onValueChange = { smtpPort = it; settingsManager.smtpPort = it },
                    label = { Text("SMTP Port") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpUser,
                    onValueChange = { smtpUser = it; settingsManager.smtpUser = it },
                    label = { Text("SMTP User") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpPass,
                    onValueChange = { smtpPass = it; settingsManager.smtpPass = it },
                    label = { Text("SMTP Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Hyvor Relay Configuration", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = hyvorApiKey,
                    onValueChange = { hyvorApiKey = it; settingsManager.hyvorApiKey = it },
                    label = { Text("Hyvor API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hyvorEndpoint,
                    onValueChange = { hyvorEndpoint = it; settingsManager.hyvorEndpoint = it },
                    label = { Text("Hyvor Endpoint URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                // SMTP user is reused as the "From" email for Hyvor Relay
                OutlinedTextField(
                    value = smtpUser,
                    onValueChange = { smtpUser = it; settingsManager.smtpUser = it },
                    label = { Text("From Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("App Masking", style = MaterialTheme.typography.titleMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("None")
                RadioButton(selected = activeMask == null, onClick = { activeMask = null; maskManager.setMask(null) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Calculator")
                RadioButton(selected = activeMask == MaskManager.MASK_CALCULATOR, onClick = { activeMask = MaskManager.MASK_CALCULATOR; maskManager.setMask(MaskManager.MASK_CALCULATOR) })
            }
        }
    }
}
