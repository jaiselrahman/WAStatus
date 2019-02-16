package com.jaiselrahman.wastatus.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.downloads.DownloadsFragment
import com.jaiselrahman.wastatus.ui.videos.VideosFragment
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
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            setContentView(R.layout.activity_main)
            viewPager.adapter = MainViewPager(supportFragmentManager)
            bottomNavigation.setupWithViewPager(viewPager)
            bottomNavigation.enableAnimation(false)
        }

        if (savedInstanceState == null) {
            requestPermission()
        }

        registerReceiver(splitResultReceiver, IntentFilter(App.ACTION_VIEW_RESULT))
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                VideosFragment.REQUEST_PERMISSION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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