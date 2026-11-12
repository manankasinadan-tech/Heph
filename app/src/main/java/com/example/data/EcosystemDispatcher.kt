package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.model.ConsoleEntry
import com.example.model.EntryStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

class EcosystemDispatcher(private val context: Context) {

    companion object {
        const val ACTION_EXECUTE = "com.hephaistos.action.EXECUTE"
        const val ACTION_RESULT = "com.hephaistos.action.RESULT"
        const val EXTRA_COMMAND = "EXTRA_COMMAND"
        const val EXTRA_PAYLOAD = "EXTRA_PAYLOAD"
        const val EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID"
        const val EXTRA_SENDER = "EXTRA_SENDER"
        const val EXTRA_RESPONSE_JSON = "EXTRA_RESPONSE_JSON"
        const val EXTRA_STATUS = "EXTRA_STATUS"
        const val NOTIFICATION_CHANNEL_ID = "hephaistos_tasks"
    }

    private val _incomingResults = MutableSharedFlow<ConsoleEntry>(extraBufferCapacity = 64)
    val incomingResults = _incomingResults.asSharedFlow()

    private val resultReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RESULT) {
                val reqId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: "req_unknown"
                val statusStr = intent.getStringExtra(EXTRA_STATUS) ?: "SUCCESS"
                val jsonResponse = intent.getStringExtra(EXTRA_RESPONSE_JSON) ?: "{}"
                val status = if (statusStr.equals("SUCCESS", ignoreCase = true)) {
                    EntryStatus.SUCCESS
                } else {
                    EntryStatus.ERROR
                }

                val entry = ConsoleEntry(
                    id = reqId,
                    command = "Réponse reçue [$reqId]",
                    status = status,
                    outputJson = jsonResponse,
                    targetModule = intent.`package` ?: "module"
                )
                _incomingResults.tryEmit(entry)
                showTaskNotification("Tâche Hephaistos terminée", "Statut: $statusStr")
            }
        }
    }

    init {
        createNotificationChannel()
        val filter = IntentFilter(ACTION_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(resultReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(resultReceiver, filter)
        }
    }

    fun dispatchModuleCommand(
        targetPackage: String,
        commandName: String,
        jsonPayload: String
    ): ConsoleEntry {
        val requestId = "req_${System.currentTimeMillis()}"
        val intent = Intent(ACTION_EXECUTE).apply {
            setPackage(targetPackage)
            putExtra(EXTRA_COMMAND, commandName)
            putExtra(EXTRA_PAYLOAD, jsonPayload)
            putExtra(EXTRA_REQUEST_ID, requestId)
            putExtra(EXTRA_SENDER, context.packageName)
        }

        try {
            context.sendBroadcast(intent)
            return ConsoleEntry(
                id = requestId,
                command = "$targetPackage: $commandName",
                status = EntryStatus.RUNNING,
                outputJson = JSONObject().apply {
                    put("status", "DISPATCHED")
                    put("requestId", requestId)
                    put("targetPackage", targetPackage)
                    put("command", commandName)
                    put("payload", try { JSONObject(jsonPayload) } catch (e: Exception) { jsonPayload })
                }.toString(2),
                targetModule = targetPackage
            )
        } catch (e: Exception) {
            return ConsoleEntry(
                id = requestId,
                command = "$targetPackage: $commandName",
                status = EntryStatus.ERROR,
                outputJson = JSONObject().apply {
                    put("status", "ERROR")
                    put("error", e.message ?: "Failed to dispatch intent")
                }.toString(2),
                targetModule = targetPackage
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hephaistos Actions"
            val descriptionText = "Notifications de progression et d'achèvement des modules Hephaistos"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(title: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (e: Exception) {
            // Notification permission might not be granted yet, ignore safely
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(resultReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
