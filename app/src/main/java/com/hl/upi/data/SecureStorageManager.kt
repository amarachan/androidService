package com.hl.upi.data

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream

class SecureStorageManager(private val context: Context) {

    // Getting the master key for file encryption
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    fun getEncryptedFile(fileName: String): EncryptedFile {
        val file = File(context.filesDir, fileName)
        return try {
            // Trying to build the encrypted file object
            buildEncryptedFile(file)
        } catch (e: Exception) {
            // If something goes wrong (like key mismatch), we log and delete corrupted file to avoid crash
            android.util.Log.e("SecureStorageManager", "Failed to build encrypted file, deleting corrupted file", e)
            if (file.exists()) file.delete()
            buildEncryptedFile(file)
        }
    }

    private fun buildEncryptedFile(file: File): EncryptedFile {
        // This is the core part where we define how to encrypt the file content
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    fun saveRecording(fileName: String, audioData: InputStream) {
        // Logic to save audio stream into encrypted file
        val encryptedFile = getEncryptedFile(fileName)
        encryptedFile.openFileOutput().use { outputStream ->
            audioData.copyTo(outputStream)
        }
    }

    fun getRecording(fileName: String): InputStream {
        // Getting input stream from the encrypted file to play it back
        val encryptedFile = getEncryptedFile(fileName)
        return encryptedFile.openFileInput()
    }

    fun deleteRecording(fileName: String) {
        // Simple delete logic
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun listRecordings(): List<String> {
        // Listing all saved recordings from the internal storage
        return context.filesDir.list()?.filter { it.endsWith(".m4a") } ?: emptyList()
    }
}
