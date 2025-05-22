package com.pvarki.deployapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {
    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)



    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("bearer_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("bearer_token", null)
    }
}