package com.example.remember.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.remember.data.Repository
import com.example.remember.ui.utils.sendNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: Repository
    ) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dueCards = repository.getDueCards().first()

            if (dueCards.isNotEmpty()) {
                sendNotification(
                    context = context,
                    title = "Time to Review!",
                    message = "You have ${dueCards.size} cards waiting for you."
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}