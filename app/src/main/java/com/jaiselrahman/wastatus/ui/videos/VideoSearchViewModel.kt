package com.jaiselrahman.wastatus.ui.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.jaiselrahman.wastatus.data.VideoSearchDataSource
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.ui.base.BaseViewModel

class VideoSearchViewModel constructor(
    private val searchDataSourceFactory: VideoSearchDataSource.Factory
) : ViewModel(), BaseViewModel {
    private var livePagedList: LiveData<PagedList<Video>>
    private var status: LiveData<Status>

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PlaylistViewModel.PAGE_SIZE)
            .setInitialLoadSizeHint(PlaylistViewModel.PAGE_SIZE)
            .setPrefetchDistance(PlaylistViewModel.PREFETCH_SIZE)
            .build()

        status = Transformations.switchMap(searchDataSourceFactory.liveDataSource()) {
            it.status
        }
        livePagedList = LivePagedListBuilder(searchDataSourceFactory, config)
            .build()
    }

    override fun getLivePagedList(): LiveData<PagedList<Video>> {
        return livePagedList
    }

    override fun getStatus(): LiveData<Status> {
        return status
    }

    override fun reset() {
        searchDataSourceFactory.reset()
    }
}