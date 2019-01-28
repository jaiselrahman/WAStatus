package com.jaiselrahman.wastatus.data.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaiselrahman.wastatus.model.Video

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideos(videos: List<Video>)

    @Query("SELECT * FROM Video ORDER BY publishedAt DESC")
    fun getVideos(): DataSource.Factory<Int, Video>
}