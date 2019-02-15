package com.jaiselrahman.wastatus.ui.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.jaiselrahman.wastatus.data.VideoDataSource
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.model.Video

class VideosViewModel : ViewModel() {
    private var dataSourceFactory: VideoDataSource.Factory
    private var livePagedList: LiveData<PagedList<Video>>
    private var status: LiveData<Status>

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PAGE_SIZE)
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPrefetchDistance(PREFETCH_SIZE)
            .build()

        dataSourceFactory = VideoDataSource.Factory(PAGE_SIZE.toLong())
        status = Transformations.switchMap(dataSourceFactory.liveDataSource()) {
            it.status
        }

        livePagedList = LivePagedListBuilder(dataSourceFactory, config)
            .build()
    }

    fun getLivePagedList(): LiveData<PagedList<Video>> {
        return livePagedList
    }

    fun getStatus(): LiveData<Status> {
        return status
    }

    fun reset() {
        dataSourceFactory.reset()
    }

    companion object {
        internal const val PAGE_SIZE = 20
        internal const val PREFETCH_SIZE = 5
    }
}