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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.ui.base.BaseViewModel
import com.jaiselrahman.wastatus.ui.playlist.PlaylistViewModel
import com.jaiselrahman.wastatus.ui.playlist.PlaylistViewModel.Companion.PAGE_SIZE
import com.jaiselrahman.wastatus.ui.search.VideoSearchViewModel
import com.jaiselrahman.wastatus.util.NetworkUtil
import com.jcodecraeer.xrecyclerview.LoadingMoreFooter
import com.jcodecraeer.xrecyclerview.XRecyclerView
import kotlinx.android.synthetic.main.video_lists.view.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

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
            getViewModel<PlaylistViewModel>{ parametersOf(PAGE_SIZE) }
        } else {
            getViewModel<VideoSearchViewModel>{ parametersOf(arguments?.getString(SEARCH) ?: "", PAGE_SIZE) }
        }

        videosViewModel.getLivePagedList().observe(viewLifecycleOwner, Observer {
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
                    videoList.setLoadingMoreEnabled(false)
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
                    .setType("text/plain")
                    .setChooserTitle(getString(R.string.share_video))
                    .apply { intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
                    .startChooser()

                ViewType.YOUTUBE ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url)))
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
//        if (videoList.size <= 0 || App.isShownTapTargetForVideos) return
//        TapTargetView.showFor(
//            activity, TapTarget.forView(
//                videoList[0].rootView.download,
//                getString(R.string.download),
//                getString(R.string.download_desc)
//            )
//        )
//        App.isShownTapTargetForVideos = true
    }

    override fun onDestroy() {
        super.onDestroy()
        videoList.destroy()
    }

    companion object {
        const val SEARCH = "SEARCH"
    }
}
