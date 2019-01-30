package com.jaiselrahman.wastatus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.youtube_player.*

class YouTubePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.youtube_player)

        val videoId = intent?.getStringExtra(VIDEO_ID)
        if (videoId.isNullOrBlank()) {
            Log.i(App.TAG, "Invalid Video Id")
            finish()
            return
        }

        youtubePlayer.playerUIController.showFullscreenButton(false)
        youtubePlayer.initialize({
            it.addListener(object : YouTubePlayerListener {
                override fun onPlaybackQualityChange(playbackQuality: PlayerConstants.PlaybackQuality) {}

                override fun onVideoDuration(duration: Float) {}

                override fun onCurrentSecond(second: Float) {}

                override fun onVideoLoadedFraction(loadedFraction: Float) {}

                override fun onPlaybackRateChange(playbackRate: PlayerConstants.PlaybackRate) {}

                override fun onVideoId(videoId: String) {}

                override fun onApiChange() {}

                override fun onStateChange(state: PlayerConstants.PlayerState) {}

                override fun onError(error: PlayerConstants.PlayerError) {
                    Log.i(App.TAG, error.name)
                    Toast.makeText(this@YouTubePlayerActivity, R.string.error_failed_to_play, Toast.LENGTH_SHORT).show()
                }

                override fun onReady() {
                    it.loadVideo(videoId, 0f)
                }
            })
        }, true)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayer.release()
    }

    companion object {
        const val VIDEO_ID = "VIDEO_ID"
        fun play(context: Context, videoId: String): Intent {
            val intent = Intent(context, YouTubePlayerActivity::class.java)
            intent.putExtra(VIDEO_ID, videoId)
            context.startActivity(intent)
            return intent
        }
    }
}