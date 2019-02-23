package com.jaiselrahman.wastatus.ui.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.jaiselrahman.wastatus.data.PlaylistDataSource
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.ui.base.BaseViewModel

class PlaylistViewModel : ViewModel(), BaseViewModel {
    private var dataSourceFactory: PlaylistDataSource.Factory
    private var livePagedList: LiveData<PagedList<Video>>
    private var status: LiveData<Status>

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PAGE_SIZE)
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPrefetchDistance(PREFETCH_SIZE)
            .build()

        dataSourceFactory = PlaylistDataSource.Factory(PAGE_SIZE.toLong())
        status = Transformations.switchMap(dataSourceFactory.liveDataSource()) {
            it.status
        }

        livePagedList = LivePagedListBuilder(dataSourceFactory, config)
            .build()
    }

    override fun getLivePagedList(): LiveData<PagedList<Video>> {
        return livePagedList
    }

    override fun getStatus(): LiveData<Status> {
        return status
    }

    override fun reset() {
        dataSourceFactory.reset()
    }

    companion object {
        internal const val PAGE_SIZE = 20
        internal const val PREFETCH_SIZE = 5
    }
}