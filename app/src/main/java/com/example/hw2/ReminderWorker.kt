package com.example.hw2

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.hw2.data.detail
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val CHANNEL_NAME = "Reminder"
    }

    override fun doWork(): Result {
        val allPlans = queryDataFromDatabase(applicationContext)
        val currenttime = System.currentTimeMillis()


        val upcomingPlans = allPlans.filter { plan ->
            plan.begindate?.let { it != 0.toLong() && it - currenttime < 86400000 && it > currenttime && !plan.isChecked }
                ?: false
        }

        val overPlans = allPlans.filter { plan ->
            plan.enddate?.let { it != 0.toLong() && it < currenttime && !plan.isChecked } ?: false
        }

        upcomingPlans.forEach {
            showNotification(
                applicationContext,
                it,
                "你的${it.title}计划即将开始"
            )
        }
        overPlans.forEach {
            showNotification(
                applicationContext,
                it,
                "你的${it.title}计划已经结束，请输入完成情况"
            )
        }

        return Result.success()
    }

    private fun showNotification(context: Context, plan: detail, contentText: String) {
        val notificationId = plan.id ?: System.currentTimeMillis().toInt()
        val notificationManager = createNotificationChannel(context)
        val builder = buildNotification(context, contentText)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(context: Context): NotificationManager {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationManager
    }

    private fun buildNotification(
        context: Context,
        contentText: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_reminder)
            .setContentTitle("Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}