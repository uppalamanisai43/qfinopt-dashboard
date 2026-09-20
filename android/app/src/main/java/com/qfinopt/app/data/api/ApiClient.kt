package com.qfinopt.app.data.api

import com.qfinopt.app.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var currentBaseUrl: String = AppConfig.BASE_URL
    private var apiInstance: QFinOptApi? = null

    private fun buildOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Synchronized
    fun getApi(): QFinOptApi {
        if (apiInstance == null) {
            apiInstance = buildRetrofit(currentBaseUrl).create(QFinOptApi::class.java)
        }
        return apiInstance!!
    }

    @Synchronized
    fun setBaseUrl(newUrl: String) {
        val cleanUrl = newUrl.trim().removeSuffix("/")
        if (cleanUrl.isNotBlank()) {
            currentBaseUrl = cleanUrl
            apiInstance = buildRetrofit(currentBaseUrl).create(QFinOptApi::class.java)
        }
    }

    fun getBaseUrl(): String = currentBaseUrl
}
