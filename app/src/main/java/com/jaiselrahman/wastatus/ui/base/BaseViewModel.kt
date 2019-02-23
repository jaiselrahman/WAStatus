package com.jaiselrahman.wastatus.ui.base

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.jaiselrahman.wastatus.data.api.Status
import com.jaiselrahman.wastatus.model.Video

interface BaseViewModel {
    fun getLivePagedList(): LiveData<PagedList<Video>>

    fun getStatus(): LiveData<Status>

    fun reset()
}