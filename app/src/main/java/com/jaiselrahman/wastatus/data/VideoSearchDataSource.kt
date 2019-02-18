package com.jaiselrahman.wastatus.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.data.api.VideoSearchApi
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.util.NetworkUtil

class VideoSearchDataSource(private val query: String, pageSize: Long) : PageKeyedDataSource<String, Video>() {
    init {
        VideoSearchApi.pageSize = pageSize
    }

    val status = MutableLiveData<Status>()
    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Video>) {
        val videos = getVideos()
        callback.onResult(
            videos,
            VideoSearchApi.searchResultToken.prevToken,
            VideoSearchApi.searchResultToken.nextToken
        )
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Video>) {
        val videos = getVideos(params.key)
        callback.onResult(
            videos,
            VideoSearchApi.searchResultToken.nextToken
        )
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Video>) {
        val videos = getVideos(params.key)
        callback.onResult(
            videos,
            VideoSearchApi.searchResultToken.prevToken
        )
    }

    private fun getVideos(key: String? = null): List<Video> {
        if (!NetworkUtil.isNetworkAvailable()) return listOf()
        postValue(Status.START)
        lateinit var videos: List<Video>
        try {
            videos = VideoSearchApi.searchVideos(query, key)
            Log.i(App.TAG, "Search Loaded ${videos.size} videos")
            postValue(Status.SUCCESS)
        } catch (e: Exception) {
            videos = listOf()
            postValue(Status.ERROR)
            Log.e(App.TAG, e.message, e)
        }

        if (VideoSearchApi.isLoaded)
            postValue(Status.COMPLETE)
        return videos
    }

    private fun postValue(newStatus: Status) {
        if (status.value != newStatus) {
            status.postValue(newStatus)
        }
    }

    class Factory(private val query: String, private val pageSize: Long) : DataSource.Factory<String, Video>() {
        private var videoDataSource: VideoSearchDataSource? = null
        private var liveDataSource = MutableLiveData<VideoSearchDataSource>()

        override fun create(): DataSource<String, Video> {
            videoDataSource = VideoSearchDataSource(query, pageSize)
            videoDataSource?.addInvalidatedCallback {
                VideoSearchApi.reset()
            }
            liveDataSource.postValue(videoDataSource)
            return videoDataSource!!
        }

        fun reset() {
            VideoSearchApi.reset()
            videoDataSource?.invalidate()
        }

        fun liveDataSource(): LiveData<VideoSearchDataSource> {
            return liveDataSource
        }
    }
}