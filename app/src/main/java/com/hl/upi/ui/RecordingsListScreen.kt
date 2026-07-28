package com.hl.upi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hl.upi.data.SecureStorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsListScreen(navController: NavController) {
    val context = LocalContext.current
    val storageManager = remember { SecureStorageManager(context) }
    var recordings by remember { mutableStateOf(storageManager.listRecordings()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (recordings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No recordings found")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(recordings) { recording ->
                    ListItem(
                        headlineContent = { Text(recording) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { 
                                    // Playback logic would go here
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                }
                                IconButton(onClick = { 
                                    storageManager.deleteRecording(recording)
                                    recordings = storageManager.listRecordings()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
