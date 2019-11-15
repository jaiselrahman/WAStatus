package com.jaiselrahman.wastatus.di

import com.jaiselrahman.wastatus.data.api.PlaylistVideoApi
import com.jaiselrahman.wastatus.data.api.VideoSearchApi
import org.koin.dsl.module

val apiModule = module {
    single { PlaylistVideoApi }

    single { VideoSearchApi }
}