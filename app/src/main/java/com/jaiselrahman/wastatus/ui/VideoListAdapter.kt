package com.jaiselrahman.wastatus.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.model.Video
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.video_list_item.view.*

class VideoListAdapter : PagedListAdapter<Video, VideoListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.video_list_item, parent, false)
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
        holder.bind(payloads[0] as Map<String, Int>)
    }

    class ViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(video: Video) {
            v.title.text = video.title
            v.viewCount.text = video.viewCount.toString()
            v.likeCount.text = video.likeCount.toString()
            Picasso.get()
                .load(video.thumbnail)
                .into(v.thumbnail)
        }

        fun bind(payload: Map<String, Int>) {
            if (payload.containsKey(VIEW_COUNT)) {
                v.viewCount.text = payload[VIEW_COUNT].toString()
            }
            if (payload.containsKey(LIKE_COUNT)) {
                v.likeCount.text = payload[LIKE_COUNT].toString()
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
                val payload = HashMap<String, Int>()

                if (oldItem.viewCount != newItem.viewCount)
                    payload[VIEW_COUNT] = newItem.viewCount

                if (oldItem.likeCount != newItem.likeCount)
                    payload[LIKE_COUNT] = newItem.likeCount

                return payload
            }
        }
    }
}
