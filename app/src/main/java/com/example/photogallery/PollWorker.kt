package com.example.photogallery

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"
class PollWorker(
    private val context: Context,
    workerParameters: WorkerParameters
):  CoroutineWorker( context, workerParameters ) {
    override suspend fun  doWork(): Result {
        Log.i(TAG, "WORK REQUEST TRIGGERED")
        return Result.success()
    }
}

