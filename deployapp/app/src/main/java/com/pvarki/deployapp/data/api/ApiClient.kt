package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.App
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
   private const val BASE_URL = "https://casual-halibut.solution.dev.pvarki.fi"
   //private const val BASE_URL = "https://localmaeher.dev.pvarki.fi:4439"


    // Fake token provider for demo purposes
    private fun getToken(): String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(::getToken))

        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(App.AppPrefs.restApiBaseUrl) // get url from preferences
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)

        .build()
}