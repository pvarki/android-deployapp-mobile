package com.pvarki.deployapp.data.api

import android.content.Context
import com.pvarki.deployapp.App.Companion.AppPrefs
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import com.pvarki.deployapp.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


object MtlsApiClient {
    @Volatile
    private var retrofitInstance: Retrofit? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // if (Utils().isDebuggerAttached())
        setLevel(
            HttpLoggingInterceptor.Level.BODY
        )
    }

    fun getRetrofit(
        certInputStream: InputStream,
        certPassword: String,
        context: Context
    ): Retrofit {
        if (retrofitInstance == null || AppPrefs.restApiBaseUrl != retrofitInstance?.baseUrl()
                .toString()
        ) {
            retrofitInstance = Retrofit.Builder()
                .baseUrl("https://mtls.busy-leopard.solution.dev.pvarki.fi")
                .addConverterFactory(GsonConverterFactory.create())
                .client(createMutualTlsHttpClient(certInputStream, certPassword, context))
                .build()
        }
        return retrofitInstance!!
    }

    fun reset() {
        retrofitInstance = null
    }


    fun loadCustomTrustManager(context: Context): Array<TrustManager> {
        val certificateFactory = CertificateFactory.getInstance("X.509")

        val pfxFilePath = Utils().getCertDirectory(context) + "/ca.crt"
        val caInput: InputStream = File(pfxFilePath).inputStream()

        val ca = certificateFactory.generateCertificate(caInput)
        caInput.close()

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        return tmf.trustManagers
    }


    fun createSslContext(
        pfxInputStream: InputStream,
        pfxPassword: String,
        trustManagers: Array<TrustManager>
    ): SSLContext {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(pfxInputStream, pfxPassword.toCharArray())
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, pfxPassword.toCharArray())

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, trustManagers, SecureRandom())
        return sslContext
    }

    fun createMutualTlsHttpClient(
        certInputStream: InputStream,
        certPassword: String,
        context: Context
    ): OkHttpClient {

        val trustManagers = loadCustomTrustManager(context)

        // Create SSLContext
        val sslContext = createSslContext(certInputStream, certPassword, trustManagers)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManagers[0] as X509TrustManager)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}