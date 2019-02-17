package com.jaiselrahman.wastatus.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.MainActivity
import com.jaiselrahman.wastatus.util.OnTrimVideoListener
import com.jaiselrahman.wastatus.util.VideoUtils
import java.io.File

class VideoSplitterService : IntentService("VideoSplitterService") {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        notificationManager = App.getNotificationManager()
        notificationBuilder = NotificationCompat.Builder(this, App.DOWNLOAD_NOTIFY_CHANNEL)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(0, 0, true)
            .setContentTitle(getString(R.string.splitting))
    }

    override fun onHandleIntent(intent: Intent?) {
        val videoPath = intent?.getStringExtra(App.VIDEO_PATH)
        if (videoPath == null) {
            Log.i(App.TAG, "Invalid VideoPath NULL")
            return
        }
        val videoFile = File(videoPath)
        notificationBuilder.setContentText(videoFile.nameWithoutExtension)
        startForeground(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
        try {
            val duration = VideoUtils.getDuration(videoFile)
            val segmentTime = if (duration % 30 <= 10) 20 else 29
            val segments = (duration / segmentTime).let { if (duration % segmentTime == 0L) it - 1 else it }

            Log.i(App.TAG, "Duration : $duration, SegmentTime : $segmentTime, Segments : $segments")

            for (i in 0..segments) {
                val startTime = i * (segmentTime + 1) * 1000
                val endTime = if (i != segments) (i + 1) * (segmentTime) * 1000 else (duration) * 1000
                VideoUtils.startTrim(
                    videoFile, getSegmentDestFile(videoFile, i),
                    startTime, endTime,
                    object : OnTrimVideoListener {
                        override fun onTrimStarted() {
                            Log.i(App.TAG, "onTrimStarted $i")
                        }

                        override fun getResult(uri: Uri) {
                            Log.i(App.TAG, "getResult $i: ${uri.path}")
                        }

                        override fun cancelAction() {
                            Log.i(App.TAG, "cancelAction $i")
                        }

                        override fun onError(message: String) {
                            Log.e(App.TAG, "onError $i: $message")
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e(App.TAG, "Split Failed", e)
            stopForeground(true)
            updateNotification(getString(R.string.split_failed), videoFile, false)
            return
        }
        Log.i(App.TAG, "Split done")
        stopForeground(true)
        updateNotification(getString(R.string.splitting_done), videoFile, true)
        val viewIntent = Intent(App.ACTION_VIEW_RESULT)
            .putExtra(MainActivity.VIDEO_PATH, videoPath)
            .putExtra(MainActivity.SPLITTED_VIDEOS, true)
        sendBroadcast(viewIntent)
    }

    private fun getSegmentDestFile(videoFile: File, segment: Long): File {
        return File(
            App.getAppDir(),
            "${videoFile.nameWithoutExtension}${"%02d".format(segment)}${VideoUtils.VIDEO_EXT}"
        )
    }

    private fun updateNotification(
        title: String = getString(R.string.splitting),
        videoFile: File? = null,
        success: Boolean = false
    ) {
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(videoFile?.nameWithoutExtension)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setOngoing(false)
        notificationBuilder.setProgress(0, 0, false)
        if (success) {
            val viewAction = getViewAction(videoFile?.path ?: "")
            notificationBuilder.setContentIntent(viewAction.actionIntent)
        }
        notificationManager.notify(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
    }

    private fun getViewAction(videoPath: String): NotificationCompat.Action {
        val viewIntent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.VIDEO_PATH, videoPath)
            .putExtra(MainActivity.SPLITTED_VIDEOS, true)
        val viewPendingIntent =
            PendingIntent.getActivity(
                this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        return NotificationCompat.Action(R.drawable.ic_view, getString(R.string.view), viewPendingIntent)
    }
}
