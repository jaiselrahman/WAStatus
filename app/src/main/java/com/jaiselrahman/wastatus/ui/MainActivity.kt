package com.jaiselrahman.wastatus.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.util.NetworkUtil
import com.jcodecraeer.xrecyclerview.LoadingMoreFooter
import com.jcodecraeer.xrecyclerview.XRecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var videoListAdapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        R.string.listview_loading

        videoListAdapter = VideoListAdapter()
        videoList.layoutManager = LinearLayoutManager(this)
        videoList.adapter = videoListAdapter
        videoList.itemAnimator = null

        val dividerDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
        videoList.addItemDecoration(dividerDecoration)

        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
            return
        }
        if (!NetworkUtil.isNetworkAvailable()) {
            Log.i(App.TAG, "No network available")
            Toast.makeText(this, R.string.error_network_not_available, Toast.LENGTH_SHORT).show()
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
                            this,
                            getString(R.string.error_refresh_videos_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        Toast.makeText(this, R.string.error_network_not_available, Toast.LENGTH_SHORT).show()
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
                    YouTubePlayerActivity.play(this, video.id)

                ViewType.SHARE -> ShareCompat.IntentBuilder.from(this)
                    .setStream(Uri.parse(video.url))
                    .setType("text/plain")
                    .setChooserTitle("Share video")
                    .startChooser()

                ViewType.YOUTUBE ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url)))

                ViewType.WHATSAPP ->
                    Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    private fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@MainActivity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoList.destroy()
    }

    companion object {
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    }
}