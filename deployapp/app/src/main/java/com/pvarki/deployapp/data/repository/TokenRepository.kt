package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import retrofit2.HttpException

class TokenRepository {
    private val api: ApiService = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun refreshToken(): String {
        val response = api.refreshToken()

        if (response.isSuccessful) {
            return response.body()?.string() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
    }

}