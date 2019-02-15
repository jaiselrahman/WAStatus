package com.jaiselrahman.wastatus.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.MainActivity
import com.jaiselrahman.wastatus.util.Utils
import com.jaiselrahman.wastatus.util.VideoUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLException


class VideoDownloaderService : IntentService("VideoDownloader") {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var extractor: YouTubeExtractor? = null
    private var responseBody: ResponseBody? = null
    private var destFile: File? = null
    private val isCancelled = AtomicBoolean(false)

    private val broadcastReceiver = VideoDownloaderReceiver()

    override fun onCreate() {
        super.onCreate()
        notificationManager = App.getNotificationManager()
        registerReceiver(broadcastReceiver, IntentFilter(ACTION_CANCEL))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationBuilder = getNotificationBuilder()
            .setContentTitle(getString(R.string.downloading))
            .addAction(getCancelAction(startId))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        isCancelled.set(false)
        startForeground(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
        val videoUrl = intent?.getStringExtra(App.VIDEO_URL)
        if (videoUrl == null) {
            Log.i(App.TAG, "Video Url NULL")
            stopForeground(true)
            return
        }

        lateinit var videoTitle: String
        extractor = @SuppressLint("StaticFieldLeak")
        object : YouTubeExtractor(applicationContext) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                if (ytFiles != null && videoMeta != null) {
                    videoTitle = videoMeta.title!!
                    updateNotification(0, text = videoTitle)
                }
            }
        }

        val downloadUrl: String?
        try {
            extractor?.extract(videoUrl, false, false)
            downloadUrl = extractor?.get()?.get(TAG_3GP_240)?.url
            if (downloadUrl == null) {
                stopForeground(true)
                updateNotification(
                    title = getString(R.string.downloading_failed),
                    notificationBuilder = getNotificationBuilder(), done = true
                )
                Log.i(App.TAG, "Extracted Video Url NULL")
                return
            }
        } catch (e: Exception) {
            Log.e(App.TAG, e.message, e)
            stopForeground(true)
            return
        }

        if (isCancelled.get()) {
            stopForeground(true)
            return
        }

        val request = Request.Builder().get()
            .url(downloadUrl)
            .build()
        val response = client.newCall(request).execute()
        responseBody = response.body()
        if (!response!!.isSuccessful || responseBody == null) {
            Log.e(App.TAG, "Response" + response.message())
            stopForeground(true)
            updateNotification(
                title = getString(R.string.downloading_failed), text = videoTitle,
                notificationBuilder = getNotificationBuilder(), done = true
            )
            return
        }

        destFile = File(App.getAppDir(), "$videoTitle${VideoUtils.VIDEO_EXT}")
        try {
            Utils.writeResponseToFile(responseBody!!, destFile!!) {
                updateNotification(it, if (it == 100) "Downloaded" else "Downloading... $it%", videoTitle)
            }
        } catch (e: SSLException) {
            Log.e(App.TAG, e.message, e)
            stopForeground(true)
            if (destFile!!.exists()) destFile!!.delete()
            updateNotification(
                title = getString(R.string.downloading_failed), text = videoTitle,
                notificationBuilder = getNotificationBuilder(), done = true
            )
            return
        } catch (e: Exception) {
            Log.e(App.TAG, e.message, e)
            stopForeground(true)
            return
        }

        if (isCancelled.get()) {
            stopForeground(true)
            return
        }

        stopForeground(true)

        val viewAction = getViewAction(destFile!!.path)
        updateNotification(
            title = getString(R.string.downloaded), text = videoTitle,
            notificationBuilder = getNotificationBuilder()
                .addAction(getSplitAction(destFile!!.path))
                .addAction(viewAction)
                .setContentIntent(viewAction.actionIntent),
            done = true
        )
        Log.i(App.TAG, "Service finished")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun getCancelAction(startId: Int): NotificationCompat.Action {
        val cancelIntent = Intent(ACTION_CANCEL)
            .putExtra(START_ID, startId)
        val cancelPendingIntent =
            PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.cancel), cancelPendingIntent)
    }

    private fun getViewAction(videoPath: String): NotificationCompat.Action {
        val viewIntent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.VIDEO_PATH, videoPath)
        val viewPendingIntent = PendingIntent.getActivity(
            this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action(R.drawable.ic_view, getString(R.string.view), viewPendingIntent)
    }

    private fun getSplitAction(path: String): NotificationCompat.Action {
        val trimIntent = Intent(this, VideoSplitterService::class.java)
            .putExtra(App.VIDEO_PATH, path)
        val trimPendingIntent =
            PendingIntent.getService(this, 0, trimIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_split, getString(R.string.split), trimPendingIntent)
    }

    private var lastUpdateTime = 0L
    private fun updateNotification(
        progress: Int? = null,
        title: String = getString(R.string.downloading),
        text: String? = null,
        notificationBuilder: NotificationCompat.Builder = this.notificationBuilder,
        done: Boolean = false
    ) {
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(text)

        if (progress == null) {
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setOngoing(false)
            notificationBuilder.setProgress(0, 0, false)
            notificationManager.notify(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
            return
        }

        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setOngoing(true)
        if (done) {

            notificationBuilder.setProgress(0, 0, false)
            notificationManager.notify(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
        } else {
            val currentTime = System.currentTimeMillis()
            if (progress == 100 || currentTime - lastUpdateTime > UPDATE_INTERVAL) {
                notificationBuilder.setProgress(100, progress, false)
                notificationManager.notify(App.DOWNLOAD_NOTIFY_ID, notificationBuilder.build())
                lastUpdateTime = currentTime
            }
        }
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(App.getContext(), App.DOWNLOAD_NOTIFY_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(0, 0, true)
    }

    inner class VideoDownloaderReceiver : BroadcastReceiver() {
        private val executor = Executors.newSingleThreadExecutor()

        override fun onReceive(context: Context?, intent: Intent?) {
            isCancelled.set(true)
            extractor?.cancel(true)
            executor.execute {
                try {
                    responseBody?.close()
                } catch (e: Exception) {
                    Log.e(App.TAG, e.message, e)
                }
            }
            if (destFile != null && destFile!!.exists()) {
                destFile!!.delete()
            }
            stopSelfResult(intent?.getIntExtra(START_ID, -1) ?: -1)
        }
    }

    companion object {
        private const val ACTION_CANCEL = "com.jaiselrahman.wastatus.CANCEL"
        private const val START_ID = "START_ID"
        private const val TAG_3GP_240 = 18 //18 MP4_360
        private const val UPDATE_INTERVAL = 1000

        private val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .build()
        }
    }
}
