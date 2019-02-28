package com.jaiselrahman.wastatus.ui.downloads

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.service.VideoSplitterService
import com.jaiselrahman.wastatus.util.VideoUtils
import kotlinx.android.synthetic.main.downloaded_list_item.view.*
import kotlinx.android.synthetic.main.view_videos.*
import kotlinx.android.synthetic.main.view_videos.view.*
import java.io.File
import java.io.FileFilter

class DownloadsFragment : Fragment() {
    private lateinit var videoList: RecyclerView
    private lateinit var videoListAdapter: DownloadsAdapter
    private var origVideos = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.view_videos, container, false)
        videoList = view.videoList
        activity?.actionBar?.setDisplayShowHomeEnabled(true)
        App.getNotificationManager().cancel(App.DOWNLOAD_NOTIFY_ID)

        origVideos = arguments?.getBoolean(App.ORIG_VIDEOS, true) ?: true

        videoListAdapter = DownloadsAdapter(origVideos) { file, buttonType ->
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
                    Toast.makeText(context, R.string.check_split_status, Toast.LENGTH_SHORT).show()
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
        videoList.layoutManager = LinearLayoutManager(context)
        videoList.adapter = videoListAdapter

        videoListAdapter.setFiles(getVideos())

        view.swipeRefresh.setOnRefreshListener {
            videoListAdapter.setFiles(getVideos())
            swipeRefresh.isRefreshing = false
        }
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (view != null && isVisibleToUser) {
            if (videoListAdapter.itemCount <= 0) {
                videoListAdapter.setFiles(getVideos())
            }
            showTapTargets()
        }
    }

    private fun showTapTargets() {
        if (videoList.size <= 0 || App.isShownTapTargetForDownloads) return
        TapTargetSequence(activity).targets(
            TapTarget.forView(
                videoList[0].rootView.split,
                getString(R.string.split),
                getString(R.string.split_desc)
            ), TapTarget.forView(
                videoList[0].rootView.whatsapp,
                getString(R.string.open_in_whatsapp),
                getString(R.string.open_in_whatsapp_desc)
            )
        ).continueOnCancel(true).start()
        App.isShownTapTargetForDownloads = true
    }

    private fun getVideos(): List<File> {
        val fileRegex = if (origVideos) {
            Regex(".*[^0-9]${VideoUtils.VIDEO_EXT}$")
        } else {
            val filePath = arguments?.getString(App.VIDEO_PATH) ?: ""
            val fileName = filePath.substringAfterLast("/").substringBefore(".")
            Regex("${Regex.escape(fileName)}[0-9]{2}${VideoUtils.VIDEO_EXT}$")
        }

        return App.getAppDir().listFiles(FileFilter { it.name.matches(fileRegex) })?.sortedBy { it.name } ?: emptyList()
    }

    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
    }
}
