package com.jaiselrahman.wastatus.data.api

import com.jaiselrahman.wastatus.model.Video

object VideoSearchApi {
    var pageSize = 50L
    val searchResultToken = PageToken(null, null)

    var isLoaded: Boolean = false
        private set

    fun reset() {
        isLoaded = false
    }

    fun searchVideos(query: String, token: String? = null): List<Video> {
        val searchResult = ApiUtil.youTubeService.search()
            .list("snippet").apply {
                maxResults = pageSize
                fields = "items/id/videoId,nextPageToken"
                q = query
                type = "video"
                key = ApiUtil.API_KEY
                pageToken = token
            }.execute()

        searchResultToken.prevToken = searchResult.prevPageToken
        searchResultToken.nextToken = searchResult.nextPageToken

        isLoaded = searchResult.nextPageToken == null

        val vIds = searchResult.items.map { it.id.videoId }

        return ApiUtil.getVideos(vIds)
    }

    class PageToken(var prevToken: String?, var nextToken: String?)
}