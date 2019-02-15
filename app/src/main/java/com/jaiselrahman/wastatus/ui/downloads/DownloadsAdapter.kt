package com.jaiselrahman.wastatus.ui.downloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaiselrahman.wastatus.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.downloaded_list_item.view.*
import java.io.File

class DownloadsAdapter(
    private val showSplitAction: Boolean,
    private val onFileClickListener: OnFileClickListener
) : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {
    private var files: Array<File>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.downloaded_list_item, parent, false),
            onFileClickListener,
            showSplitAction
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files!![position])
    }

    override fun getItemCount() = files?.size ?: 0

    fun setFiles(files: Array<File>) {
        this.files = files
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val v: View,
        private val onFileClickListener: OnFileClickListener,
        showSplitAction: Boolean
    ) : RecyclerView.ViewHolder(v) {
        private var file: File? = null

        init {
            v.thumbnail.setOnClickListener {
                file?.let { onFileClickListener(it, ButtonType.THUMBNAIL) }
            }
            v.share.setOnClickListener {
                file?.let { onFileClickListener(it, ButtonType.SHARE) }
            }
            v.whatsapp.setOnClickListener {
                file?.let { onFileClickListener(it, ButtonType.WHATSAPP) }
            }
            if (showSplitAction) {
                v.split.visibility = View.VISIBLE
                v.split.setOnClickListener {
                    file?.let { onFileClickListener(it, ButtonType.SPLIT) }
                }
            }
        }

        fun bind(file: File) {
            this.file = file
            v.title.text = file.name
            Picasso.get().load(file).into(v.thumbnail)
        }
    }

    enum class ButtonType {
        THUMBNAIL, SHARE, WHATSAPP, SPLIT
    }
}

internal typealias OnFileClickListener = (File, DownloadsAdapter.ButtonType) -> Unit
