package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.App.Companion.AppPrefs
import com.pvarki.deployapp.utils.PreferenceHelper.jwt
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import com.pvarki.deployapp.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    @Volatile
    private var retrofitInstance: Retrofit? = null

    private fun getToken(): String = AppPrefs.jwt

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
       // if (Utils().isDebuggerAttached())
            setLevel(
                HttpLoggingInterceptor.Level.BODY
            )
    }

    fun getRetrofit(): Retrofit {
        if (retrofitInstance == null || AppPrefs.restApiBaseUrl != retrofitInstance?.baseUrl()
                .toString()
        ) {
            retrofitInstance = Retrofit.Builder()
                .baseUrl(AppPrefs.restApiBaseUrl)
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