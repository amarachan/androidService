package com.hl.upi.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hl.upi.data.SettingsManager
import com.hl.upi.util.HyvorRelayClient
import com.hl.upi.util.ZipUtils
import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EmailWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val settingsManager = SettingsManager(context)

    override suspend fun doWork(): Result {
        // Getting the filename from input data
        val fileName = inputData.getString("file_name") ?: return Result.failure()
        val audioFile = File(applicationContext.filesDir, fileName)

        // Making sure the file actually exists before doing anything
        if (!audioFile.exists()) {
            Log.e("EmailWorker", "File not found: ${audioFile.absolutePath}")
            return Result.failure()
        }

        // 1. Creating a ZIP file to save space and bundle things properly
        val zipFileName = "${fileName.removeSuffix(".m4a")}.zip"
        val zipFile = File(applicationContext.filesDir, zipFileName)
        try {
            ZipUtils.zipFile(audioFile, zipFile)
        } catch (e: Exception) {
            Log.e("EmailWorker", "Failed to ZIP file", e)
            return Result.failure()
        }

        // Checking where to send the email
        val targetEmail = settingsManager.targetEmail ?: return Result.success()
        
        // Choosing the delivery method based on user settings
        return if (settingsManager.useHyvorRelay) {
            sendViaHyvorRelay(targetEmail, zipFile)
        } else {
            sendViaSmtp(targetEmail, zipFile)
        }
    }

    private suspend fun sendViaHyvorRelay(targetEmail: String, file: File): Result = suspendCoroutine { continuation ->
        // Logic to send email using Hyvor Relay API
        val apiKey = settingsManager.hyvorApiKey
        if (apiKey == null) {
            Log.e("EmailWorker", "Hyvor Relay API Key not set")
            continuation.resume(Result.failure())
            return@suspendCoroutine
        }

        val client = HyvorRelayClient(apiKey, settingsManager.hyvorEndpoint)
        val fromEmail = settingsManager.smtpUser ?: "relay@hyvor.com"

        client.sendEmailWithAttachment(
            from = fromEmail,
            to = targetEmail,
            subject = "New Call Recording (ZIP): ${file.name}",
            html = "<p>Attached is a new call recording compressed in ZIP format.</p>",
            attachmentFile = file
        ) { success, error ->
            if (success) {
                Log.d("EmailWorker", "Email sent successfully via Hyvor Relay")
                // Deleting the ZIP file once it's sent successfully
                file.delete()
                continuation.resume(Result.success())
            } else {
                Log.e("EmailWorker", "Hyvor Relay send failed: $error")
                // If it fails, we tell WorkManager to try again later
                continuation.resume(Result.retry())
            }
        }
    }

    private fun sendViaSmtp(targetEmail: String, file: File): Result {
        // Standard SMTP sending logic using JavaMail
        val host = settingsManager.smtpHost
        val port = settingsManager.smtpPort
        val user = settingsManager.smtpUser ?: return Result.failure()
        val pass = settingsManager.smtpPass ?: return Result.failure()

        // Setting up mail server properties
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
        }

        // Creating session with authentication
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(user, pass)
            }
        })

        return try {
            // Preparing the email message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(user))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail))
                subject = "New Call Recording (ZIP): ${file.name}"
                
                val messageBodyPart = MimeBodyPart().apply {
                    setText("Attached is a new call recording compressed in ZIP format.")
                }

                // Attaching the ZIP file here
                val attachmentBodyPart = MimeBodyPart().apply {
                    val source = FileDataSource(file)
                    dataHandler = DataHandler(source)
                    this.fileName = file.name
                }

                val multipart = MimeMultipart().apply {
                    addBodyPart(messageBodyPart)
                    addBodyPart(attachmentBodyPart)
                }

                setContent(multipart)
            }

            // Sending the actual email
            Transport.send(message)
            Log.d("EmailWorker", "Email sent successfully via SMTP with ${file.name}")
            // Cleaning up the ZIP file after success
            file.delete()
            Result.success()
        } catch (e: MessagingException) {
            Log.e("EmailWorker", "Failed to send email via SMTP", e)
            // Retry logic if network is down or server is busy
            Result.retry()
        }
    }
}
