package com.pvarki.deployapp.data.repository

import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.AllProductsInstructionFiles

class InstructionsRepository {
    private val api = ApiClient.getRetrofit().create(ApiService::class.java)


    suspend fun userInstructionFragment(): AllProductsInstructionFiles {
        val response = api.userInstructionFragment()
        return response
    }
}