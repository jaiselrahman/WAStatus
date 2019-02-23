package com.jaiselrahman.wastatus.ui.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.model.Video
import com.jaiselrahman.wastatus.util.compactString
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.video_list_item.view.*

class VideoListAdapter : PagedListAdapter<Video, VideoListAdapter.ViewHolder>(DIFF_CALLBACK) {
    private var onVideoClickListener: OnVideoClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.video_list_item, parent, false),
            onVideoClickListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        @Suppress("UNCHECKED_CAST")
        holder.bind(payloads[0] as Map<String, Long>)
    }

    fun setOnVideoClickListener(onVideoClickListener: OnVideoClickListener?) {
        this.onVideoClickListener = onVideoClickListener
    }

    class ViewHolder(
        private val v: View,
        private var onVideoClickListener: OnVideoClickListener? = null
    ) : RecyclerView.ViewHolder(v) {
        private lateinit var video: Video

        fun bind(video: Video) {
            this.video = video
            v.title.text = video.title
            v.viewCount.text = video.viewCount.compactString()
            v.likeCount.text = video.likeCount.compactString()
            Picasso.get()
                .load(video.thumbnail)
                .into(v.thumbnail)

            v.thumbnail.setOnClickListener {
                onVideoClickListener?.invoke(video, ViewType.THUMBNAIL)
            }

            v.share.setOnClickListener {
                onVideoClickListener?.invoke(video, ViewType.SHARE)
            }

            v.youtube.setOnClickListener {
                onVideoClickListener?.invoke(video, ViewType.YOUTUBE)
            }

            v.download.setOnClickListener {
                onVideoClickListener?.invoke(video, ViewType.DOWNLOAD)
            }
        }

        fun bind(payload: Map<String, Long>) {
            if (payload.containsKey(VIEW_COUNT)) {
                v.viewCount.text = payload[VIEW_COUNT].compactString()
            }
            if (payload.containsKey(LIKE_COUNT)) {
                v.likeCount.text = payload[LIKE_COUNT].compactString()
            }
        }
    }

    companion object {
        private const val VIEW_COUNT = "VIEW_COUNT"
        private const val LIKE_COUNT = "LIKE_COUNT"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: Video, newItem: Video): Any? {
                val payload = HashMap<String, Long>()

                if (oldItem.viewCount != newItem.viewCount)
                    payload[VIEW_COUNT] = newItem.viewCount

                if (oldItem.likeCount != newItem.likeCount)
                    payload[LIKE_COUNT] = newItem.likeCount

                return payload
            }
        }
    }
}

internal typealias OnVideoClickListener = (Video, ViewType) -> Unit

enum class ViewType {
    THUMBNAIL, SHARE, YOUTUBE, DOWNLOAD
}

