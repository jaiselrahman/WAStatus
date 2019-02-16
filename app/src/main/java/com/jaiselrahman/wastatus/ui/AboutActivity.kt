package com.jaiselrahman.wastatus.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.jaiselrahman.wastatus.R
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        share.setOnClickListener {
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(getString(R.string.share_text))
                .startChooser()
        }
        author.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:${getString(R.string.app_author_email)}")))
        }
        licenses.setOnClickListener {
            startActivity(Intent(this@AboutActivity, LicenseActivity::class.java))
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