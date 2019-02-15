package com.jaiselrahman.wastatus.util

import android.net.Uri
import org.mp4parser.IsoFile
import org.mp4parser.muxer.Track
import org.mp4parser.muxer.builder.DefaultMp4Builder
import org.mp4parser.muxer.container.mp4.MovieCreator
import org.mp4parser.muxer.tracks.AppendTrack
import org.mp4parser.muxer.tracks.ClippedTrack
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object VideoUtils {
    const val VIDEO_EXT = ".mp4"
    const val VIDEO_MIME = "video/mp4"

    fun getDuration(file: File): Long {
        val isoFile = IsoFile(file)
        return isoFile.movieBox.movieHeaderBox.duration / isoFile.movieBox.movieHeaderBox.timescale
    }

    @Throws(IOException::class)
    fun startTrim(src: File, dest: File, startMs: Long, endMs: Long, callback: OnTrimVideoListener) {
        genVideoUsingMp4Parser(src, dest, startMs, endMs, callback)
    }

    @Throws(IOException::class)
    private fun genVideoUsingMp4Parser(
        src: File,
        dst: File,
        startMs: Long,
        endMs: Long,
        callback: OnTrimVideoListener
    ) {
        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
        // Otherwise we get OOM with large movie files.

        val movie = MovieCreator.build(src.absolutePath)

        val tracks = movie.tracks
        movie.tracks = LinkedList<Track>()
        // remove all tracks we will create new tracks from the old

        var startTime1 = (startMs / 1000).toDouble()
        val endTime1 = (endMs / 1000).toDouble()

        var timeCorrected = false

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (track in tracks) {
            if (track.syncSamples != null && track.syncSamples.isNotEmpty()) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.")
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false)
//                endTime1 = correctTimeToSyncSample(track, endTime1, true)
                timeCorrected = true
            }
        }

        for (track in tracks) {
            var currentTime = 0.0
            var lastTime = -1.0
            var startSample1: Long = -1
            var endSample1: Long = -1

            for ((currentSample, i) in (0 until track.sampleDurations.size).withIndex()) {
                val delta = track.sampleDurations[i]


                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample.toLong()
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample.toLong()
                }
                lastTime = currentTime
                currentTime += (delta.toDouble() / track.trackMetaData.timescale)
            }
            movie.addTrack(AppendTrack(ClippedTrack(track, startSample1, endSample1)))
        }

        dst.parentFile.mkdirs()

        if (!dst.exists()) {
            dst.createNewFile()
        }


        val out = DefaultMp4Builder().build(movie)
//        val d = ((endTime1 - startTime1) * out.movieBox.movieHeaderBox.timescale).toLong()
//        Log.i(App.TAG, "Duration : $d")
//        out.movieBox.movieHeaderBox.duration = d

        val fos = FileOutputStream(dst)
        val fc = fos.channel
        out.writeContainer(fc)

        fc.close()
        fos.close()

        callback.getResult(Uri.parse(dst.toString()))
    }

    private fun correctTimeToSyncSample(track: Track, cutHere: Double, next: Boolean): Double {
        val timeOfSyncSamples = DoubleArray(track.syncSamples.size)
        var currentTime = 0.0
        for ((currentSample, i) in (0 until track.sampleDurations.size).withIndex()) {
            val delta = track.sampleDurations[i]

            if (Arrays.binarySearch(track.syncSamples, (currentSample + 1).toLong()) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.syncSamples, (currentSample + 1).toLong())] = currentTime
            }
            currentTime += (delta.toDouble() / track.trackMetaData.timescale)

        }
        var previous = 0.0
        for (timeOfSyncSample in timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                return if (next) {
                    timeOfSyncSample
                } else {
                    previous
                }
            }
            previous = timeOfSyncSample
        }
        return timeOfSyncSamples[timeOfSyncSamples.size - 1]
    }
}