package com.jaiselrahman.wastatus.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.data.api.PlaylistVideoApi
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.data.db.DB
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.util.NetworkUtil

class PlaylistDataSource(pageSize: Long) : PositionalDataSource<Video>() {
    private val videoDao = DB.videoDao
    private val dataSource = videoDao.getVideos().create() as PositionalDataSource<Video>

    init {
        PlaylistVideoApi.pageSize = pageSize
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
            val videos = PlaylistVideoApi.loadVideos(pos)
            Log.i(App.TAG, "Loaded ${videos.size} videos")
            videoDao.insertVideos(videos)
            postValue(Status.SUCCESS)
        } catch (e: Exception) {
            postValue(Status.ERROR)
            Log.e(App.TAG, e.message, e)
        }

        if (PlaylistVideoApi.isLoaded)
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
        private var videoLoadDataSource: PlaylistDataSource? = null
        private var liveDataSource = MutableLiveData<PlaylistDataSource>()

        override fun create(): DataSource<Int, Video> {
            videoLoadDataSource = PlaylistDataSource(pageSize)
            videoLoadDataSource?.addInvalidatedCallback {
                PlaylistVideoApi.reset()
            }
            liveDataSource.postValue(videoLoadDataSource)
            return videoLoadDataSource!!
        }

        fun reset() {
            videoLoadDataSource?.invalidate()
        }

        fun liveDataSource(): LiveData<PlaylistDataSource> {
            return liveDataSource
        }
    }
}