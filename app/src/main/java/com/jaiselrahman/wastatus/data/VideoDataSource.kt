package com.jaiselrahman.wastatus.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.data.api.ApiService
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.data.db.DB
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.util.NetworkUtil

class VideoDataSource(pageSize: Long) : PositionalDataSource<Video>() {
    private val videoDao = DB.videoDao
    private val dataSource = videoDao.getVideos().create() as PositionalDataSource<Video>

    init {
        ApiService.pageSize = pageSize
    }

    val status = MutableLiveData<Status>()

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadFromNetwork()
        dataSource.loadInitial(params, callback)
    }


    override fun loadRange(
        params: PositionalDataSource.LoadRangeParams,
        callback: PositionalDataSource.LoadRangeCallback<Video>
    ) {
        if (params.startPosition % params.loadSize != 0) {
            return
        }
        loadFromNetwork(params.startPosition)
        dataSource.loadRange(params, callback)
    }

    private fun loadFromNetwork(pos: Int = 0) {
        if (!NetworkUtil.isNetworkAvailable()) return
        postValue(Status.START)
        try {
            val videos = ApiService.loadVideos(pos)
            Log.i(App.TAG, "Loaded ${videos.size} videos")
            videoDao.insertVideos(videos)
            postValue(Status.SUCCESS)
        } catch (e: Exception) {
            postValue(Status.ERROR)
            Log.e(App.TAG, e.message, e)
        }

        if (ApiService.isLoaded)
            postValue(Status.COMPLETE)
    }

    override fun invalidate() {
        super.invalidate()
        dataSource.invalidate()
    }

    private fun postValue(newStatus: Status) {
        if (status.value != newStatus) {
            status.postValue(newStatus)
        }
    }

    class Factory(private val pageSize: Long) : DataSource.Factory<Int, Video>() {
        private var videoDataSource: VideoDataSource? = null
        private var liveDataSource = MutableLiveData<VideoDataSource>()

        override fun create(): DataSource<Int, Video> {
            videoDataSource = VideoDataSource(pageSize)
            videoDataSource?.addInvalidatedCallback {
                ApiService.reset()
            }
            liveDataSource.postValue(videoDataSource)
            return videoDataSource!!
        }

        fun reset() {
            videoDataSource?.invalidate()
        }

        fun liveDataSource(): LiveData<VideoDataSource> {
            return liveDataSource
        }
    }
}