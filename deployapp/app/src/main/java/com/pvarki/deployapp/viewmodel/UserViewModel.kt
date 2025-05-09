package com.pvarki.deployapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.data.api.ApiService
import com.pvarki.deployapp.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserViewModel : ViewModel() {
    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    private val _users = MutableStateFlow<Result<List<User>>>(Result.success(emptyList()))
    val users: StateFlow<Result<List<User>>> = _users

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    _users.value = Result.success(response.body() ?: emptyList())
                } else {
                    _users.value = Result.failure(HttpException(response))
                }
            } catch (e: Exception) {
                _users.value = Result.failure(e)
            }
        }
    }
}