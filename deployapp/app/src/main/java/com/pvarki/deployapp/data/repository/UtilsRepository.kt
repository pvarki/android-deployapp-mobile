package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.EnrollRequest
import retrofit2.HttpException

class UtilsRepository {
    private val api: ApiService = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun getJwtPubkey(): String {
        val response = api.getJwtPubkey()

        if (response.isSuccessful) {
            return response.body()?.string() ?: throw IllegalStateException("Response body is null")
        } else {
            throw HttpException(response)
        }
    }


}