package com.example.calibre_zenith.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.calibre_zenith.ui.theme.screens.DynamicPlannerTask
import java.util.Calendar

class TaskAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTaskAlerts(task: DynamicPlannerTask) {
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, task.year)
            set(Calendar.DAY_OF_YEAR, task.dayOfYear)
            set(Calendar.HOUR_OF_DAY, task.startHour)
            set(Calendar.MINUTE, task.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val taskStartTimeMs = targetCalendar.timeInMillis
        val currentTimeMs = System.currentTimeMillis()

        // 1. Schedule T-30 Minute Warning ticket
        val thirtyMinBeforeMs = taskStartTimeMs - (10* 1000)
        if (thirtyMinBeforeMs > currentTimeMs) {
            val intent30m = createIntent(task, "TYPE_30M")
            val pendingIntent30m = PendingIntent.getBroadcast(
                context, task.id.hashCode() + 30, intent30m,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, thirtyMinBeforeMs, pendingIntent30m)
        }

        // 2. Schedule T-5 Minute Cognitive Launchpad ticket
        val fiveMinBeforeMs = taskStartTimeMs - (20 * 1000)
        if (fiveMinBeforeMs > currentTimeMs) {
            val intent5m = createIntent(task, "TYPE_5M")
            val pendingIntent5m = PendingIntent.getBroadcast(
                context, task.id.hashCode() + 5, intent5m,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fiveMinBeforeMs, pendingIntent5m)
        }
    }

    private fun createIntent(task: DynamicPlannerTask, alarmType: String): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_MICRO", task.microStep)
            putExtra("TASK_FRICTION", task.frictionNotes)
            putExtra("ALARM_TYPE", alarmType)
        }
    }
}