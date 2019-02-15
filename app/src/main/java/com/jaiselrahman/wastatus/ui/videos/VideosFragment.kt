package com.jaiselrahman.wastatus.ui.videos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.service.VideoDownloaderService
import com.jaiselrahman.wastatus.ui.YouTubePlayerActivity
import com.jaiselrahman.wastatus.util.NetworkUtil
import com.jcodecraeer.xrecyclerview.LoadingMoreFooter
import com.jcodecraeer.xrecyclerview.XRecyclerView
import kotlinx.android.synthetic.main.video_lists.view.*

class VideosFragment : Fragment() {
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var videoList: XRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.video_lists, container, false)
        videoList = view.videoList
        videoListAdapter = VideoListAdapter()
        videoList.layoutManager = LinearLayoutManager(context)
        videoList.adapter = videoListAdapter
        videoList.itemAnimator = null

        val dividerDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        dividerDecoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider)!!)
        videoList.addItemDecoration(dividerDecoration)

        if (!NetworkUtil.isNetworkAvailable()) {
            Log.i(App.TAG, "No network available")
            Toast.makeText(context, R.string.error_network_not_available, Toast.LENGTH_SHORT).show()
        }

        val videosViewModel = ViewModelProviders.of(this)
            .get(VideosViewModel::class.java)

        videosViewModel.getLivePagedList().observe(this, Observer {
            videoListAdapter.submitList(it)
            videoList.post {
                videoList.scrollToPosition(0)
            }
        })

        videosViewModel.getStatus().observe(this, Observer {
            Log.i(App.TAG, "Status $it")
            when (it) {
                Status.ERROR -> {
                    if (NetworkUtil.isNetworkAvailable())
                        Toast.makeText(
                            context,
                            getString(R.string.error_refresh_videos_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        Toast.makeText(context, R.string.error_network_not_available, Toast.LENGTH_SHORT).show()
                }
                Status.COMPLETE -> {
                    videoList.setLoadingMoreEnabled(false)
                }
                Status.START -> {
                    videoList.setLoadingMoreEnabled(true)
                }
                else -> {
                    videoList.refreshComplete()
                    videoList.loadMoreComplete()
                }
            }
        })

        videoList.defaultFootView.setState(LoadingMoreFooter.STATE_LOADING)
        videoList.setLoadingListener(object : XRecyclerView.LoadingListener {
            override fun onLoadMore() {}

            override fun onRefresh() {
                videosViewModel.reset()
            }
        })

        videoListAdapter.setOnVideoClickListener { video, buttonType ->
            when (buttonType) {
                ViewType.THUMBNAIL ->
                    YouTubePlayerActivity.play(context!!, video.id)

                ViewType.SHARE -> ShareCompat.IntentBuilder.from(activity)
                    .setStream(Uri.parse(video.url))
                    .setType("text/plain")
                    .setChooserTitle(getString(R.string.share_video))
                    .apply { intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
                    .startChooser()

                ViewType.YOUTUBE ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url)))

                ViewType.DOWNLOAD -> {
                    if (savedInstanceState == null) {
                        requestPermission()
                    }
                    val intent = Intent(context, VideoDownloaderService::class.java)
                        .putExtra(App.VIDEO_URL, video.url)
                    ContextCompat.startForegroundService(context!!, intent)
                }
            }
        }
        return view
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (activity?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(context, getString(R.string.request_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoList.destroy()
    }

    companion object {
        internal const val REQUEST_PERMISSION = 1003
    }
}