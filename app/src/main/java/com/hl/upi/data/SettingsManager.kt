package com.hl.upi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsManager(context: Context) {

    // Initializing the master key for secure shared preferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        // Trying to create or get the encrypted prefs
        createPrefs(context)
    } catch (e: Exception) {
        // If it fails (maybe keys are messed up), we delete the file and recreate it
        android.util.Log.e("SettingsManager", "Failed to create encrypted prefs, recreating...", e)
        val sharedPrefsFile = java.io.File(context.filesDir.parent, "shared_prefs/upi_secure_prefs.xml")
        if (sharedPrefsFile.exists()) {
            sharedPrefsFile.delete()
        }
        createPrefs(context)
    }

    private fun createPrefs(context: Context): SharedPreferences {
        // This is the recommended way to store sensitive settings in Android
        return EncryptedSharedPreferences.create(
            context,
            "upi_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        // Keys for all our settings
        private const val KEY_EMAIL = "target_email"
        private const val KEY_SMTP_HOST = "smtp_host"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SMTP_USER = "smtp_user"
        private const val KEY_SMTP_PASS = "smtp_pass"
        private const val KEY_PIN = "app_pin"
        private const val KEY_IS_SERVICE_ENABLED = "service_enabled"
        private const val KEY_ACTIVE_MASK = "active_mask"
        private const val KEY_USE_HYVOR_RELAY = "use_hyvor_relay"
        private const val KEY_HYVOR_API_KEY = "hyvor_api_key"
        private const val KEY_HYVOR_ENDPOINT = "hyvor_endpoint"
    }

    // Properties to easily get and set values from anywhere in the app
    var targetEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var smtpHost: String
        get() = prefs.getString(KEY_SMTP_HOST, "smtp.gmail.com") ?: "smtp.gmail.com"
        set(value) = prefs.edit().putString(KEY_SMTP_HOST, value).apply()

    var smtpPort: String
        get() = prefs.getString(KEY_SMTP_PORT, "587") ?: "587"
        set(value) = prefs.edit().putString(KEY_SMTP_PORT, value).apply()

    var smtpUser: String?
        get() = prefs.getString(KEY_SMTP_USER, null)
        set(value) = prefs.edit().putString(KEY_SMTP_USER, value).apply()

    var smtpPass: String?
        get() = prefs.getString(KEY_SMTP_PASS, null)
        set(value) = prefs.edit().putString(KEY_SMTP_PASS, value).apply()

    var appPin: String?
        get() = prefs.getString(KEY_PIN, null)
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_SERVICE_ENABLED, value).apply()

    var activeMask: String?
        get() = prefs.getString(KEY_ACTIVE_MASK, null)
        set(value) = prefs.edit().putString(KEY_ACTIVE_MASK, value).apply()

    var useHyvorRelay: Boolean
        get() = prefs.getBoolean(KEY_USE_HYVOR_RELAY, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_HYVOR_RELAY, value).apply()

    var hyvorApiKey: String?
        get() = prefs.getString(KEY_HYVOR_API_KEY, null)
        set(value) = prefs.edit().putString(KEY_HYVOR_API_KEY, value).apply()

    var hyvorEndpoint: String
        get() = prefs.getString(KEY_HYVOR_ENDPOINT, "https://relay.hyvor.com/api/v1/send") ?: "https://relay.hyvor.com/api/v1/send"
        set(value) = prefs.edit().putString(KEY_HYVOR_ENDPOINT, value).apply()
}
