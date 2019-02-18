package com.jaiselrahman.wastatus.ui.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.jaiselrahman.wastatus.data.VideoSearchDataSource
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.model.Video

class VideoSearchViewModel private constructor(query: String) : ViewModel(), BaseViewModel {
    private var searchDataSourceFactory: VideoSearchDataSource.Factory
    private var livePagedList: LiveData<PagedList<Video>>
    private var status: LiveData<Status>

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PlaylistViewModel.PAGE_SIZE)
            .setInitialLoadSizeHint(PlaylistViewModel.PAGE_SIZE)
            .setPrefetchDistance(PlaylistViewModel.PREFETCH_SIZE)
            .build()

        searchDataSourceFactory = VideoSearchDataSource.Factory(query, PlaylistViewModel.PAGE_SIZE.toLong())
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

    class Factory(private val query: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            try {
                @Suppress("UNCHECKED_CAST")
                return VideoSearchViewModel(query) as T
            } catch (e: Exception) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        }
    }

}