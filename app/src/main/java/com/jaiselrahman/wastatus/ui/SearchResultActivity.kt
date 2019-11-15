package com.jaiselrahman.wastatus.ui

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.ui.videos.VideosFragment

class SearchResultActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(savedInstanceState == null)
            handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val query = when (intent.action) {
            Intent.ACTION_SEARCH -> {
                intent.getStringExtra(SearchManager.QUERY)
            }
            Intent.ACTION_VIEW -> {
                intent.dataString ?: ""
            }
            Intent.ACTION_SEND -> {
                if (intent.hasExtra(Intent.EXTRA_TEXT))
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                else
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM).toString()
            }
            else -> {
                Toast.makeText(this, "Invalid search", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, VideosFragment().apply {
                arguments = Bundle().apply {
                    putString(VideosFragment.SEARCH, query)
                }
            }).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = (menu.findItem(R.id.search).actionView as SearchView).also {
            it.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, SearchResultActivity::class.java)))
        }
        searchView.setQuery(intent.getStringExtra(SearchManager.QUERY), false)
        return true
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
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}