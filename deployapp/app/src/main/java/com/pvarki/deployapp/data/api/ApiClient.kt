package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.App
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {


    @Volatile
    private var retrofitInstance: Retrofit? = null

    private fun getToken(): String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    fun getRetrofit(): Retrofit {
        if (retrofitInstance == null || App.AppPrefs.restApiBaseUrl != retrofitInstance?.baseUrl().toString()) {
            retrofitInstance = Retrofit.Builder()
                .baseUrl(App.AppPrefs.restApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
        }
        return retrofitInstance!!
    }

    fun reset() {
        retrofitInstance = null
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(::getToken))

        .addInterceptor(loggingInterceptor)
        .build()

  /*  val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(App.AppPrefs.restApiBaseUrl) // get url from preferences
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)

        .build()*/
}