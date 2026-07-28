package com.hl.upi.util

import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class HyvorRelayClient(
    private val apiKey: String,
    private val endpoint: String = "https://relay.hyvor.com/api/v1/send"
) {
    private val client = OkHttpClient()

    fun sendEmailWithAttachment(
        from: String,
        to: String,
        subject: String,
        html: String,
        attachmentFile: File,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val attachmentBytes = attachmentFile.readBytes()
            val base64Content = Base64.encodeToString(attachmentBytes, Base64.NO_WRAP)

            val attachment = JSONObject().apply {
                put("name", attachmentFile.name)
                put("type", "application/zip")
                put("content", base64Content)
            }

            val json = JSONObject().apply {
                put("from", from)
                put("to", to)
                put("subject", subject)
                put("html", html)
                put("attachments", JSONArray().put(attachment))
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("X-API-KEY", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(false, e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, "Error: ${response.code} - ${response.body?.string()}")
                    }
                }
            })
        } catch (e: Exception) {
            callback(false, e.message)
        }
    }
}
