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
        if (query.startsWith("http")) {
            val vId = Companion.getVideoId(query)
            return if (vId != null) {
                isLoaded = true
                ApiUtil.getVideos(listOf(vId))
            } else {
                emptyList()
            }
        }
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

    object Companion {
        private val regex by lazy {
            Regex("^((?:https?:)?//)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(/(?:[\\w\\-]+\\?v=|embed/|v/)?)([\\w\\-]+)(\\S+)?\$")
        }

        fun getVideoId(url: String) = regex.find(url)?.groups?.get(5)?.value
    }
}