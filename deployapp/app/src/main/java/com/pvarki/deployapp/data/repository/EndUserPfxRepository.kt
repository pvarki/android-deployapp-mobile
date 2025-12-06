package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import okhttp3.ResponseBody
import retrofit2.Response

class EndUserPfxRepository {

    private val api = ApiClient.getRetrofit().create(ApiService::class.java)

    suspend fun getUserPfx(callSign: String): Response<ResponseBody> {
        val response = api.getUserPfx(callSign)
        return response
    }

    suspend fun getUserPem(callSign: String): Response<ResponseBody> {
        val response = api.getUserPem(callSign)
        return response
    }
}