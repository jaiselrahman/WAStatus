package com.jaiselrahman.wastatus.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Video(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val thumbnail: String,
    val publishedAt: Long,
    val viewCount: Long,
    val likeCount: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    )

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(url)
        parcel.writeString(thumbnail)
        parcel.writeLong(publishedAt)
        parcel.writeLong(viewCount)
        parcel.writeLong(likeCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel): Video {
            return Video(parcel)
        }

        override fun newArray(size: Int): Array<Video?> {
            return arrayOfNulls(size)
        }
    }
}
