package com.example.projectpp.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.projectpp.util.NotificationHelper

class ExpirationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Ambil data nama dan ID
        val foodName = inputData.getString("food_name") ?: return Result.failure()
        val foodId = inputData.getInt("food_id", 0)

        // Ambil pesan kustom, atau gunakan default jika null
        val customMessage = inputData.getString("message_body")
        val finalMessage = customMessage ?: "Produk '$foodName' akan segera kadaluwarsa."

        // Panggil fungsi notifikasi
        NotificationHelper.showNotification(
            context = applicationContext,
            title = "Peringatan Kadaluwarsa!",
            message = finalMessage,
            notificationId = foodId
        )

        return Result.success()
    }
}