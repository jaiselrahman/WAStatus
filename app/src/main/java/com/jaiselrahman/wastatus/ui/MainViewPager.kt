package com.jaiselrahman.wastatus.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jaiselrahman.wastatus.ui.downloads.DownloadsFragment
import com.jaiselrahman.wastatus.ui.videos.VideosFragment

class MainViewPager(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> VideosFragment()
            1 -> DownloadsFragment()
            else -> throw IllegalStateException("Position should be than $PAGE_SIZE: given $position")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Videos"
            1 -> "Downloads"
            else -> null
        }
    }

    override fun getCount() = PAGE_SIZE

    companion object {
        const val PAGE_SIZE = 2
    }
}