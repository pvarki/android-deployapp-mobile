package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.LoginCodeRequest
import retrofit2.HttpException

class TokenRepository {
    private val api = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun exchangeToken(): String {
        val response = api.exchangeToken()
        return response.body()?.string() ?: throw HttpException(response)
    }

    suspend fun refreshToken(): String {
        val response = api.refreshToken()
        return response.body()?.string() ?: throw HttpException(response)
    }

    suspend fun exchangeCode(code:String): String {
        val response = api.exchangeCode(LoginCodeRequest(code))
        return response.body()?.string() ?: throw HttpException(response)
    }



}