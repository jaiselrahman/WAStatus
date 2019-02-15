package com.jaiselrahman.wastatus.ui.downloads

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.service.VideoSplitterService
import com.jaiselrahman.wastatus.util.VideoUtils
import kotlinx.android.synthetic.main.view_videos.*
import kotlinx.android.synthetic.main.view_videos.view.*
import java.io.File
import java.io.FileFilter

class DownloadsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.view_videos, container, false)
        val videoList = view.videoList
        activity?.actionBar?.setDisplayShowHomeEnabled(true)
        App.getNotificationManager().cancel(App.DOWNLOAD_NOTIFY_ID)

        val origVideos = arguments?.getBoolean(App.ORIG_VIDEOS, true) ?: true

        videoList.layoutManager = LinearLayoutManager(context)
        videoList.adapter = DownloadsAdapter(origVideos) { file, buttonType ->
            when (buttonType) {
                DownloadsAdapter.ButtonType.THUMBNAIL -> {
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                        .setDataAndType(App.getFileUri(file), VideoUtils.VIDEO_MIME)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    startActivity(viewIntent)
                }
                DownloadsAdapter.ButtonType.SPLIT -> {
                    val splitIntent = Intent(context, VideoSplitterService::class.java)
                        .putExtra(App.VIDEO_PATH, file.path)
                    ContextCompat.startForegroundService(context!!, splitIntent)
                }
                DownloadsAdapter.ButtonType.SHARE -> {
                    ShareCompat.IntentBuilder.from(activity)
                        .setStream(App.getFileUri(file))
                        .setType(VideoUtils.VIDEO_MIME)
                        .setChooserTitle(getString(R.string.share_video))
                        .apply { intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
                        .startChooser()
                }
                DownloadsAdapter.ButtonType.WHATSAPP -> {
                    val shareIntent = ShareCompat.IntentBuilder.from(activity)
                        .setStream(App.getFileUri(file))
                        .setType(VideoUtils.VIDEO_MIME)
                        .run {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                .setPackage(WHATSAPP_PACKAGE)
                        }
                    if (shareIntent.resolveActivity(activity!!.packageManager) != null) {
                        startActivity(shareIntent)
                    } else {
                        Toast.makeText(context, getString(R.string.error_whatsapp_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        (videoList.adapter as DownloadsAdapter).setFiles(getVideos(origVideos))

        view.swipeRefresh.setOnRefreshListener {
            (videoList.adapter as DownloadsAdapter).setFiles(getVideos(origVideos))
            swipeRefresh.isRefreshing = false
        }
        return view
    }

    private fun getVideos(origVideos: Boolean): Array<File> {
        val fileRegex = if (origVideos) {
            Regex(".*[^0-9]${VideoUtils.VIDEO_EXT}$")
        } else {
            val filePath = arguments?.getString(App.VIDEO_PATH) ?: ""
            Regex("${filePath.substringAfterLast("/").substringBefore(".")}[0-9]{2}${VideoUtils.VIDEO_EXT}$")
        }

        return App.getAppDir().listFiles(FileFilter { it.name.matches(fileRegex) }) ?: emptyArray()
    }

    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
    }
}
