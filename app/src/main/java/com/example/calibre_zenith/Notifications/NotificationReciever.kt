package com.example.calibre_zenith.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val taskId = intent.getStringExtra("TASK_ID") ?: ""
        val title = intent.getStringExtra("TASK_TITLE") ?: "Objective"
        val microStep = intent.getStringExtra("TASK_MICRO") ?: ""
        val friction = intent.getStringExtra("TASK_FRICTION") ?: ""
        val alarmType = intent.getStringExtra("ALARM_TYPE") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "PLANNER_CHANNELS",
                "Mission Runway Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cognitive circuit breakers and horizon warnings."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (alarmType == "TYPE_30M") {
            val notification = NotificationCompat.Builder(context, "PLANNER_CHANNELS")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Horizon Shift: 30m Remaining")
                .setContentText("Incoming Runway Task: $title. Wrap up your current loop.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(taskId.hashCode() + 30, notification)

        } else if (alarmType == "TYPE_5M") {
            // Append data fields securely to the URI structure
            val deepLinkUri = "calibre://timer" +
                    "?taskId=${Uri.encode(taskId)}" +
                    "&title=${Uri.encode(title)}" +
                    "&microStep=${Uri.encode(microStep)}" +
                    "&friction=${Uri.encode(friction)}"

            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                taskId.hashCode(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "PLANNER_CHANNELS")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("LAUNCH RUNWAY: 5 MINUTES OUT")
                .setContentText("TRIGGER: $microStep")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "OBJECTIVE: $title\n\nLAUNCH TRIGGER: $microStep\n\nRESISTANCE PROFILE: $friction"
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(taskId.hashCode() + 5, notification)
        }
    }
}