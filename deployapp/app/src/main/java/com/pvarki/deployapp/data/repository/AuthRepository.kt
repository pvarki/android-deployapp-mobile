package com.pvarki.deployapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {
    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    suspend fun login(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.healthcheck()

             //   val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    /*val token = response.body()?.token
                    if (!token.isNullOrEmpty()) {
                        saveToken(token)
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Token is empty"))
                    }*/
                    Result.success(Unit)
                } else {
                    Result.failure(HttpException(response))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("HTTP error: ${e.message()}"))
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("bearer_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("bearer_token", null)
    }
}