package com.jaiselrahman.wastatus.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.downloads.DownloadsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val splitResultReceiver = SplitResultReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(SPLITTED_VIDEOS, false)) {
            val downloadsFragment = DownloadsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(App.ORIG_VIDEOS, false)
                    putString(App.VIDEO_PATH, intent.getStringExtra(VIDEO_PATH))
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, downloadsFragment)
                .commit()
        } else {
            setContentView(R.layout.activity_main)
            viewPager.adapter = MainViewPager(supportFragmentManager)
            bottomNavigation.setupWithViewPager(viewPager)
            bottomNavigation.enableAnimation(false)
        }

        registerReceiver(splitResultReceiver, IntentFilter(App.ACTION_VIEW_RESULT))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(splitResultReceiver)
    }

    class SplitResultReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == App.ACTION_VIEW_RESULT) {
                val viewIntent = Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.VIDEO_PATH, intent.getStringExtra(MainActivity.VIDEO_PATH))
                    .putExtra(MainActivity.SPLITTED_VIDEOS, intent.getBooleanExtra(MainActivity.VIDEO_PATH, true))
                context?.startActivity(viewIntent)
            }
        }
    }

    companion object {
        const val VIDEO_PATH = "VIDEO_PATH"
        const val SPLITTED_VIDEOS = "SPLITTED_VIDEOS"
    }
}