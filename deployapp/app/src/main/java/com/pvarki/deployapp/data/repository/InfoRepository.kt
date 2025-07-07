package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.MLTSorJwtPayload
import retrofit2.HttpException

class InfoRepository {
    private val api = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun exchangeToken(): String {
        val response = api.returnJwtPayload()
        return response.body()?.string() ?: throw HttpException(response)
    }

    suspend fun returnValiduserPayload(): MLTSorJwtPayload {
        val response = api.returnValiduserPayload()
        return response
    }

    suspend fun returnMtlsPayload(): String {
        val response = api.returnMtlsPayload()
        return response.body()?.string() ?: throw HttpException(response)
    }




}