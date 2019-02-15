package com.jaiselrahman.wastatus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.jaiselrahman.wastatus.util.VideoUtils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.File

class App : Application() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var appDir: File
    private lateinit var apiKey: String
    private lateinit var playlistId: String

    override fun onCreate() {
        super.onCreate()
        app = this

        apiKey = getString(R.string.api_key)
        playlistId = getString(R.string.playlist_id)

        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .addRequestHandler(object : RequestHandler() {
                    override fun canHandleRequest(data: Request?): Boolean {
                        return data?.uri?.path?.endsWith(VideoUtils.VIDEO_EXT) ?: false
                    }

                    override fun load(request: Request?, networkPolicy: Int): Result? {
                        val bitmap =
                            ThumbnailUtils.createVideoThumbnail(
                                request?.uri?.path,
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                        return Result(bitmap, Picasso.LoadedFrom.DISK)
                    }
                }).build()
        )

        notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                DOWNLOAD_NOTIFY_CHANNEL,
                getString(R.string.download_notifications),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        appDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + App.APP_NAME)
        if (!appDir.exists())
            appDir.mkdirs()
    }

    companion object {
        private lateinit var app: App

        private const val APP_NAME = "WAStatus"
        private const val AUTHORITY = "com.jaiselrahman.wastatus.fileprovider"

        internal const val DOWNLOAD_NOTIFY_ID = 100
        internal const val TAG = APP_NAME
        internal const val DOWNLOAD_NOTIFY_CHANNEL = "DOWNLOAD_NOTIFY_CHANNEL"
        internal const val ACTION_VIEW_RESULT = "com.jaiselrahman.wastatus"
        internal const val VIDEO_PATH = "VIDEO_PATH"
        internal const val ORIG_VIDEOS = "ORIG_VIDEOS"
        internal const val VIDEO_URL = "VIDEO_URL"

        fun getApiKey() = app.apiKey

        fun getPlaylistId() = app.playlistId

        fun getAppDir() = app.appDir

        fun getContext() = app

        fun getNotificationManager() = app.notificationManager

        fun getFileUri(file: File): Uri = FileProvider.getUriForFile(app, App.AUTHORITY, file)
    }
}