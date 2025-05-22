package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.model.EnrollResponse

class EnrollmentRepository {

    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun postEnEnrollResponse(er: EnrollRequest): EnrollResponse {
        val response = api.enrollWithCsr(er)
        return response
    }
}