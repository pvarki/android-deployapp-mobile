package com.pvarki.deployapp.data.repository


import com.pvarki.deployapp.data.model.User

import retrofit2.HttpException

class UserRepository {
   /* private val api = ApiClient.apiService

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = api.getUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }*/
}
