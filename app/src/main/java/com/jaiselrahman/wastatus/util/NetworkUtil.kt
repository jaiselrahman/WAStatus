package com.jaiselrahman.wastatus.util

import android.content.Context
import android.net.ConnectivityManager
import com.jaiselrahman.wastatus.App

class NetworkUtil {
    companion object {
        fun isNetworkAvailable(): Boolean {
            val connMgr = App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}