package com.pvarki.deployapp.data.repository

import android.content.Context
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.api.MtlsApiClient
import com.pvarki.deployapp.data.model.AllProductsHealthCheckResponse
import com.pvarki.deployapp.data.model.BasicHealthCheckResponse
import retrofit2.HttpException
import java.io.InputStream

class HealthcheckRepository {
    private val api: ApiService = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun requestHealthCheck(): BasicHealthCheckResponse {
        return api.healthcheck()
    }


    suspend fun requestHealthCheckMtls(
        certInputStream: InputStream,
        certPassword: String,
        context: Context
    ): BasicHealthCheckResponse {
        val apiMtls: ApiService =
            MtlsApiClient.getRetrofit(certInputStream, certPassword, context)
                .create(ApiService::class.java)
        return apiMtls.healthcheck()
    }


    suspend fun requestHealthCheckServices(): AllProductsHealthCheckResponse? {
        val response = api.healthcheckServices()
        if (response.isSuccessful) {
            return response.body()
        } else {
            throw HttpException(response)
        }
    }
}