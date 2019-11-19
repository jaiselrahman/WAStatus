package com.jaiselrahman.wastatus.ui.playlist

import android.Manifest
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.AboutActivity
import com.jaiselrahman.wastatus.ui.search.SearchResultActivity
import com.jaiselrahman.wastatus.ui.videos.VideosFragment

class PlayListActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            requestPermission()
        }

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, VideosFragment())
            .commit()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = (menu.findItem(R.id.search).actionView as SearchView).also {
            it.setSearchableInfo(
                searchManager.getSearchableInfo(
                    ComponentName(
                        this,
                        SearchResultActivity::class.java
                    )
                )
            )
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getString(R.string.request_permission), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        showTapTargets()
    }


    private fun showTapTargets() {
        if (!App.isShownTapTargetForSearch) {
            TapTargetView.showFor(
                this,
                TapTarget.forView(
                    searchView,
                    getString(R.string.search),
                    getString(R.string.search_desc)
                )
            )
            App.isShownTapTargetForSearch = true
        }
    }

    override fun onBackPressed() {
        if (!searchView.isIconified) {
            searchView.setQuery(null, false)
            searchView.isIconified = true
        } else {
            super.onBackPressed()
        }
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
    }

    class SplitResultReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == App.ACTION_VIEW_RESULT) {
                val viewIntent = Intent(context, PlayListActivity::class.java)
                    .putExtra(
                        VIDEO_PATH,
                        intent.getStringExtra(VIDEO_PATH)
                    )
                    .putExtra(
                        SPLITTED_VIDEOS,
                        intent.getBooleanExtra(SPLITTED_VIDEOS, true)
                    )
                context?.startActivity(viewIntent)
            }
        }
    }

    companion object {
        internal const val REQUEST_PERMISSION = 1003
        const val VIDEO_PATH = "VIDEO_PATH"
        const val SPLITTED_VIDEOS = "SPLITTED_VIDEOS"
    }
}