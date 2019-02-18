package com.jaiselrahman.wastatus.data.api

import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.data.api.ApiUtil.Companion.API_KEY
import com.jaiselrahman.wastatus.model.Video

object PlaylistVideoApi {
    private val PLAYLIST_ID = App.getPlaylistId()
    private var lastPosition = -1
    private val pageTokens = HashMap<Int, String?>()

    var pageSize = 20L

    fun reset() {
        lastPosition = -1
        pageTokens.clear()
        pageTokens[lastPosition] = null
        isLoaded = false
    }

    var isLoaded: Boolean = false
        private set

    fun loadVideos(position: Int = 0): List<Video> {
        val nextPageToken = pageTokens[lastPosition]
        lastPosition = position
        val playlist = ApiUtil.youTubeService.playlistItems()
            .list("snippet").apply {
                maxResults = pageSize
                fields = "items/snippet/resourceId/videoId,nextPageToken"
                playlistId = PLAYLIST_ID
                key = API_KEY
                pageToken = nextPageToken
            }.execute()

        pageTokens[lastPosition] = playlist.nextPageToken

        isLoaded = playlist.nextPageToken == null

        val vIds = playlist.items.map { it.snippet.resourceId.videoId }
        return ApiUtil.getVideos(vIds)
    }

    /*
    private fun getToken(limit: Long, page: Int): String {
        val start = (1 + (page - 1) * limit).toInt()
        val thirdChars = arrayOf('A', 'E', 'I', 'M', 'Q', 'U', 'Y', 'c', 'g', 'k', 'o', 's', 'w', '0', '4', '8')
        return ("C" + ('A'.toInt() + floor(start / 16.0)).toChar() + thirdChars[start % 16 - 1]) + "QAA"
    }
    */
}