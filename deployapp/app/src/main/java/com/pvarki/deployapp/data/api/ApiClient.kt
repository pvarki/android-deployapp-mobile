package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.App.Companion.AppPrefs
import com.pvarki.deployapp.utils.PreferenceHelper.jwt
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


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


    fun createMutualTlsHttpClient(
        certInputStream: InputStream,
        certPassword: String
    ): OkHttpClient {
        // Load client certificate into KeyStore
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(certInputStream, certPassword.toCharArray())

        // Create KeyManagerFactory
        val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())

        keyManagerFactory.init(keyStore, certPassword.toCharArray())

        // Create TrustManagerFactory
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Get X509TrustManager
        val trustManagers = trustManagerFactory.trustManagers
        val x509TrustManager = trustManagers
            .filterIsInstance<X509TrustManager>()
            .firstOrNull() ?: throw IllegalStateException("No X509TrustManager found")

        // Create SSLContext
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, arrayOf(x509TrustManager), null)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, x509TrustManager)
            .addInterceptor(loggingInterceptor)
            .build()

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