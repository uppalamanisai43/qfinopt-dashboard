package com.qfinopt.app.config

import android.content.Context

/**
 * Global application configuration.
 *
 * Presets:
 * 1. Wi-Fi (Host PC on same network): "http://10.0.56.180:8000"
 * 2. USB Cable (via adb reverse):     "http://127.0.0.1:8000"
 * 3. Android Emulator (Studio AVD):    "http://10.0.2.2:8000"
 */
object AppConfig {
    const val DEFAULT_WIFI_URL: String = "http://10.0.64.73:8000"
    const val DEFAULT_USB_URL: String = "http://127.0.0.1:8000"
    const val DEFAULT_EMULATOR_URL: String = "http://10.0.2.2:8000"
    const val DEFAULT_CLOUD_URL: String = "https://qfinopt.onrender.com"
    const val DEFAULT_CLOUDFLARE_URL: String = DEFAULT_CLOUD_URL

    const val BASE_URL: String = DEFAULT_WIFI_URL

    private const val PREFS_NAME = "qfinopt_config"
    private const val KEY_SERVER_URL = "server_url"

    fun getSavedBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, BASE_URL) ?: BASE_URL
    }

    fun saveBaseUrl(context: Context, url: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_URL, cleanUrl).apply()
    }
}

