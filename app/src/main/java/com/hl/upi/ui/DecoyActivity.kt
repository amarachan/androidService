package com.hl.upi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hl.upi.MainActivity
import com.hl.upi.util.MaskManager

class DecoyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val maskManager = MaskManager(this)
        setContent {
            // Functional Calculator UI as a decoy
            CalculatorUI(onSecretTrigger = {
                try {
                    // Step 1: Enable the real MainActivity
                    maskManager.enableRealApp()
                    
                    // Preparing intent to launch the real app
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    
                    // We add a small delay to make sure Android OS has time to register component state change
                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    handler.postDelayed({
                        try {
                            startActivity(intent)
                            // Note: We don't finish() here yet, let MainActivity take over properly
                        } catch (e: Exception) {
                            android.util.Log.e("DecoyActivity", "Start MainActivity failed", e)
                        }
                    }, 300) 
                    
                } catch (e: Exception) {
                    android.util.Log.e("DecoyActivity", "Error unlocking", e)
                    // Emergency fallback: just try starting it directly
                    try {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } catch (e2: Exception) {
                        // If everything fails, then God help us
                    }
                }
            })
        }
    }
}

@Composable
fun CalculatorUI(onSecretTrigger: () -> Unit) {
    // Simple state to hold calculator display value
    var display by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Calculator screen
        Text(text = display, fontSize = 48.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp))

        // Basic button layout
        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "=", "+")
        )

        buttons.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { char ->
                    Button(
                        onClick = {
                            if (char == "C") {
                                display = "0"
                            } else if (char == "=") {
                                // Checking for secret trigger code here
                                // If display is exactly "123456", we open the real app
                                if (display == "123456") {
                                    onSecretTrigger()
                                } else {
                                    display = "Error"
                                }
                            } else {
                                // Just appending numbers/operators to the display
                                if (display == "0" || display == "Error") display = char else display += char
                            }
                        },
                        modifier = Modifier.size(80.dp).padding(4.dp)
                    ) {
                        Text(text = char)
                    }
                }
            }
        }
    }
}
