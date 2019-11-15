package com.jaiselrahman.wastatus.di

import com.jaiselrahman.wastatus.ui.videos.PlaylistViewModel
import com.jaiselrahman.wastatus.ui.videos.VideoSearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {
    viewModel { (pageSize: Long) ->
        PlaylistViewModel(dataSourceFactory = get { parametersOf(pageSize) })
    }

    viewModel { (query: String, pageSize: Long) ->
        VideoSearchViewModel(searchDataSourceFactory = get { parametersOf(query, pageSize) })
    }
}