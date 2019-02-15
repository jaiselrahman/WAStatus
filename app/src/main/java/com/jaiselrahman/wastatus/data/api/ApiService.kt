package com.jaiselrahman.wastatus.data.api

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.model.Video

object ApiService {
    private val API_KEY = App.getApiKey()
    private val PLAYLIST_ID = App.getPlaylistId()
    private var youTubeService: YouTube? = null
    private var lastPosition = -1
    private val pageTokens = HashMap<Int, String?>()

    var pageSize = 20L

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        youTubeService = YouTube.Builder(
            transport, jsonFactory, null
        ).setApplicationName(App.getContext().getString(R.string.app_name))
            .build()
        reset()
    }

    fun reset() {
        lastPosition = -1
        pageTokens.clear()
        pageTokens[lastPosition] = null
        isLoaded = false
    }

    var isLoaded: Boolean = false
        private set

    fun loadVideos(position: Int = 0): List<Video> {
        val videos = ArrayList<Video>()
        val nextPageToken = pageTokens[lastPosition]
        lastPosition = position
        val playlist = youTubeService!!.playlistItems()
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

        val videoList = youTubeService!!.videos()
            .list("snippet,statistics").apply {
                fields = "items(id,snippet(title,thumbnails/default/url,publishedAt),statistics(viewCount,likeCount))"
                id = vIds.joinToString()
                key = API_KEY
            }.execute()

        videoList.items.forEach {
            videos.add(
                Video(
                    it.id,
                    it.snippet.title,
                    "https://youtube.com/watch?v=${it.id}",
                    it.snippet.thumbnails.default.url,
                    it.snippet.publishedAt.value,
                    it.statistics.viewCount.toLong(),
                    it.statistics.likeCount.toLong()
                )
            )
        }
        return videos
    }

    /*
    private fun getToken(limit: Long, page: Int): String {
        val start = (1 + (page - 1) * limit).toInt()
        val thirdChars = arrayOf('A', 'E', 'I', 'M', 'Q', 'U', 'Y', 'c', 'g', 'k', 'o', 's', 'w', '0', '4', '8')
        return ("C" + ('A'.toInt() + floor(start / 16.0)).toChar() + thirdChars[start % 16 - 1]) + "QAA"
    }
    */
}