package com.jaiselrahman.wastatus.data.api

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.model.Video

class ApiUtil {
    companion object {
        val API_KEY = App.getApiKey()

        val youTubeService: YouTube by lazy {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            YouTube.Builder(transport, jsonFactory, null)
                .setApplicationName(App.getContext().getString(R.string.app_name))
                .build()
        }

        fun getVideos(vIds: List<String>): List<Video> {
            val videos = ArrayList<Video>()
            val videoList = youTubeService.videos()
                .list("snippet,statistics").apply {
                    fields =
                        "items(id,snippet(title,thumbnails/default/url,publishedAt),statistics(viewCount,likeCount))"
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
                        it.statistics.viewCount?.toLong() ?: 0,
                        it.statistics.likeCount?.toLong() ?: 0
                    )
                )
            }
            return videos
        }
    }
}