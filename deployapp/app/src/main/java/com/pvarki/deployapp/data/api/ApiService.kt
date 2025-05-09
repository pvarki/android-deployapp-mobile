package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.data.model.HealthCheck
import com.pvarki.deployapp.data.model.LoginRequest
import com.pvarki.deployapp.data.model.LoginResponse
import com.pvarki.deployapp.data.model.MyResponse
import com.pvarki.deployapp.data.model.User
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // /api/v1/healthchec

    @GET("/api/v1/healthcheck")
    suspend fun healthcheck(): Response<HealthCheck>

    @GET("endpoint/{id}")
    fun getData(@Path("id") id: String): Call<MyResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<User>

    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

}