package com.example.projectpp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projectpp.MainActivity
import com.example.projectpp.R
import com.example.projectpp.data.FoodItem
import com.example.projectpp.worker.ExpirationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "expiration_channel"
    const val CHANNEL_NAME = "Peringatan Kadaluwarsa"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifikasi untuk produk yang akan kadaluwarsa"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Fungsi publik yang dipanggil ViewModel
    fun scheduleNotificationUnique(context: Context, food: FoodItem) {
        // 1. Jadwalkan H-3
        scheduleWorker(
            context = context,
            food = food,
            daysBefore = 3,
            uniqueWorkName = "notif_h3_${food.id}",
            customMessage = "Ingat! '${food.name}' akan kadaluwarsa dalam 3 hari."
        )

        // 2. Jadwalkan H-1
        scheduleWorker(
            context = context,
            food = food,
            daysBefore = 1,
            uniqueWorkName = "notif_h1_${food.id}",
            customMessage = "PERHATIAN! '${food.name}' akan kadaluwarsa BESOK!"
        )
    }

    // Fungsi bantuan private untuk menghitung waktu dan enqueue worker
    private fun scheduleWorker(
        context: Context,
        food: FoodItem,
        daysBefore: Int,
        uniqueWorkName: String,
        customMessage: String
    ) {
        val currentTime = System.currentTimeMillis()
        // Hitung waktu mundur (H - daysBefore)
        val triggerDate = food.expirationDate - (daysBefore * 24 * 60 * 60 * 1000L)

        // Setel notifikasi jam 7 pagi pada hari tersebut
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = triggerDate
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val delay = calendar.timeInMillis - currentTime

        // Hanya jadwalkan jika waktunya belum lewat
        if (delay > 0) {
            val data = Data.Builder()
                .putString("food_name", food.name)
                .putInt("food_id", food.id.toInt())
                .putString("message_body", customMessage)
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<ExpirationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("notif_${food.id}") // Tag umum untuk pembatalan
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }

    fun cancelNotification(context: Context, foodId: Long) {
        // Batalkan H-3
        WorkManager.getInstance(context).cancelUniqueWork("notif_h3_$foodId")
        // Batalkan H-1
        WorkManager.getInstance(context).cancelUniqueWork("notif_h1_$foodId")
        // Batalkan Test Mode (jika ada)
        WorkManager.getInstance(context).cancelUniqueWork("notif_test_work_$foodId")
    }

    fun scheduleNotificationTest(context: Context, food: FoodItem, triggerTimeMillis: Long) {
        val delay = triggerTimeMillis - System.currentTimeMillis()

        if (delay > 0) {
            val data = Data.Builder()
                .putString("food_name", food.name)
                .putInt("food_id", food.id.toInt())
                .putString("message_body", "TEST: '${food.name}' berhasil dijadwalkan!") // Pesan Test
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<ExpirationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("notif_test_${food.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "notif_test_work_${food.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }
}