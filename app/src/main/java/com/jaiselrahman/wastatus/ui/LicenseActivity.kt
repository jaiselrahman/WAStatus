package com.jaiselrahman.wastatus.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Attribution
import com.franmontiel.attributionpresenter.entities.Library
import com.franmontiel.attributionpresenter.entities.License

class LicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        setContentView(listView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        listView.adapter = AttributionPresenter.Builder(this)
            .addAttributions(
                Attribution.Builder("Android based YouTube url extractor")
                    .addCopyrightNotice("Copyright (c) 2014, Benjamin Huber")
                    .addLicense(
                        "Modified BSD",
                        "https://github.com/HaarigerHarald/android-youtubeExtractor/blob/master/LICENSE"
                    )
                    .setWebsite("https://github.com/HaarigerHarald/android-youtubeExtractor")
                    .build(),
                Attribution.Builder("android-youtube-player")
                    .addCopyrightNotice("Copyright (c) 2018 Pierfrancesco Soffritti")
                    .addLicense(License.MIT)
                    .setWebsite("https://github.com/PierfrancescoSoffritti/android-youtube-player")
                    .build(),
                Attribution.Builder("AttributionPresenter")
                    .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                    .build(),
                Attribution.Builder("BottomNavigationViewEx")
                    .addCopyrightNotice("Copyright (c) 2017 ittianyu")
                    .addLicense(License.MIT)
                    .setWebsite("https://github.com/ittianyu/BottomNavigationViewEx")
                    .build(),
                Attribution.Builder("CircleImageView")
                    .addCopyrightNotice("Copyright 2014 - 2019 Henning Dodenhof")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/hdodenhof/CircleImageView")
                    .build(),
                Attribution.Builder("MP4 Parser")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/sannies/mp4parser")
                    .build(),
                Attribution.Builder("TapTargetView")
                    .addCopyrightNotice("Copyright 2016 Keepsafe Software Inc.")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/KeepSafe/TapTargetView")
                    .build(),
                Attribution.Builder("XRecyclerView")
                    .addCopyrightNotice("Copyright 2015 jianghejie")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/XRecyclerView/XRecyclerView")
                    .build()
            ).addAttributions(
                Library.PICASSO,
                Library.OK_HTTP
            ).build().adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}