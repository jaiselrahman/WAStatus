package com.jaiselrahman.wastatus.ui.videos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.service.VideoDownloaderService
import com.jaiselrahman.wastatus.ui.base.BaseViewModel
import com.jaiselrahman.wastatus.util.NetworkUtil
import com.jcodecraeer.xrecyclerview.LoadingMoreFooter
import com.jcodecraeer.xrecyclerview.XRecyclerView
import kotlinx.android.synthetic.main.video_list_item.view.*
import kotlinx.android.synthetic.main.video_lists.view.*

class VideosFragment : Fragment() {
    private lateinit var videosViewModel: BaseViewModel
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

        videosViewModel = if (arguments?.containsKey(SEARCH) != true) {
            ViewModelProviders.of(this)
                .get(PlaylistViewModel::class.java)
        } else {
            ViewModelProviders.of(this, VideoSearchViewModel.Factory(arguments?.getString(SEARCH) ?: ""))
                .get(VideoSearchViewModel::class.java)
        }

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
                    .setText(video.url)
                    .setChooserTitle(getString(R.string.share_video))
                    .apply { intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
                    .startChooser()

                ViewType.YOUTUBE ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url)))

                ViewType.DOWNLOAD -> {
                    val intent = Intent(context, VideoDownloaderService::class.java)
                        .putExtra(App.VIDEO_URL, video.url)
                    ContextCompat.startForegroundService(context!!, intent)
                }
            }
        }
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (view != null && isVisibleToUser) {
            showTapTargets()
        }
    }

    private fun showTapTargets() {
        if (videoList.size <= 0 || App.isShownTapTargetForVideos) return
        TapTargetView.showFor(
            activity, TapTarget.forView(
                videoList[0].rootView.download,
                getString(R.string.download),
                getString(R.string.download_desc)
            )
        )
        App.isShownTapTargetForVideos = true
    }

    override fun onDestroy() {
        super.onDestroy()
        videoList.destroy()
    }

    companion object {
        const val SEARCH = "SEARCH"
    }
}