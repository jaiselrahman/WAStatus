package com.jaiselrahman.wastatus.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Video(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val thumbnail: String,
    val publishedAt: Long,
    val viewCount: Int,
    val likeCount: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Video) {
            return this.id == other.id &&
                    this.viewCount == other.viewCount &&
                    this.likeCount == other.likeCount &&
                    this.title == other.title &&
                    this.url == other.url &&
                    this.publishedAt == other.publishedAt
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
