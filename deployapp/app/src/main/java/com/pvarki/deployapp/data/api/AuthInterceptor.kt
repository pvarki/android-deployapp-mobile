package com.pvarki.deployapp.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        requestBuilder.addHeader("x-api-key", "reqres-free-v1")
        return chain.proceed(requestBuilder.build())
    }
}