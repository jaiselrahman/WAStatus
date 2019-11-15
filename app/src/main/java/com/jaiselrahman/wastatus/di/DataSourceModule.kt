package com.jaiselrahman.wastatus.di

import com.jaiselrahman.wastatus.data.PlaylistDataSource
import com.jaiselrahman.wastatus.data.VideoSearchDataSource
import org.koin.dsl.module

val dataSourceModule = module {
    factory { (pageSize: Long) ->
        PlaylistDataSource.Factory(playlistVideoApi = get(), videoDao = get(), pageSize = pageSize)
    }

    factory { (query: String, pageSize: Long) ->
        VideoSearchDataSource.Factory(videoSearchApi = get(), query = query, pageSize = pageSize)
    }
}