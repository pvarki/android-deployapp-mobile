package com.pvarki.deployapp.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object ApiClient {
    //private const val BASE_URL = "https://reqres.in/api/"
  // private const val BASE_URL = "https://localmaeher.dev.pvarki.fi:4439"
    private const val BASE_URL = "https://10.0.2.2:4439"

   // https://localmaeher.dev.pvarki.fi:4439/api/v1/healthcheck

    // Fake token provider for demo purposes
    private fun getToken(): String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Trust all certs
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Bypass hostname verification
                .build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun createUnsafeRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(::getToken))

        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient())

        .build()
}