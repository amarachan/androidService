package com.hl.upi.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.hl.upi.data.SettingsManager

class MaskManager(private val context: Context) {

    private val settingsManager = SettingsManager(context)
    private val packageManager = context.packageManager

    fun setMask(maskName: String?) {
        // Logic to switch between real app and decoy icons
        val oldMask = settingsManager.activeMask
        val newMask = maskName

        if (oldMask == newMask) return // No change needed

        // Enable new mask first to ensure user always has an entry point on home screen
        if (newMask == null) {
            setComponentEnabled(".MainActivity", true)
        } else {
            setComponentEnabled(newMask, true)
        }

        // Now disabling the old one
        if (oldMask == null) {
            setComponentEnabled(".MainActivity", false)
        } else {
            setComponentEnabled(oldMask, false)
        }

        // Saving the current state
        settingsManager.activeMask = newMask
    }

    /**
     * Specialized unmask for transition from decoy to real app.
     * We only enable the real app here, won't disable decoy yet to keep process alive.
     */
    fun enableRealApp() {
        setComponentEnabled(".MainActivity", true)
    }

    /**
     * Completes the unmasking process by disabling the decoy.
     * We call this from MainActivity once it's up and running.
     */
    fun disableDecoy() {
        val oldMask = settingsManager.activeMask
        if (oldMask != null) {
            setComponentEnabled(oldMask, false)
            settingsManager.activeMask = null
        }
    }

    private fun setComponentEnabled(className: String, enabled: Boolean) {
        // Helper to enable or disable an activity/alias component
        val pkg = context.packageName
        val cls = if (className.startsWith(".")) "$pkg$className" else className
        val componentName = ComponentName(pkg, cls)
        
        android.util.Log.d("MaskManager", "Setting component $cls to enabled=$enabled")
        
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        try {
            // This is where the magic happens - changing the launcher icon dynamically
            packageManager.setComponentEnabledSetting(
                componentName,
                state,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            android.util.Log.e("MaskManager", "Failed to set component state", e)
        }
    }

    companion object {
        const val MASK_CALCULATOR = ".CalculatorAlias"
    }
}
