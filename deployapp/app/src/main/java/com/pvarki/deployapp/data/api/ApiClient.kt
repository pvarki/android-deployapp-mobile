package com.pvarki.deployapp.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    //private const val BASE_URL = "https://reqres.in/api/"
   private const val BASE_URL = "https://localmaeher.dev.pvarki.fi:4439"

   // https://localmaeher.dev.pvarki.fi:4439/api/v1/healthcheck

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
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)

        .build()
}