package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.data.model.AllProductsHealthCheckResponse
import com.pvarki.deployapp.data.model.BasicHealthCheckResponse
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.model.EnrollResponse
import com.pvarki.deployapp.data.model.LoginRequest
import com.pvarki.deployapp.data.model.LoginResponse
import com.pvarki.deployapp.data.model.MyResponse
import com.pvarki.deployapp.data.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {



    @GET("/api/v1/token/jwt/refresh")
    suspend fun refreshToken(): Response<ResponseBody>

    @POST("/api/v1/token/jwt/exchange")
    suspend fun exchangeToken(): Response<ResponseBody>

//  {"invite_code": inv_code, "callsign": callsign, "csr": csrpem}
    @POST("/api/v1/enrollment/invitecode/enroll")
    suspend fun enrollWithCsr(@Body enrollment: EnrollRequest): EnrollResponse

    @GET("/api/v1/utils/jwt.pub")
    suspend fun getJwtPubkey(): Response<ResponseBody>

    @GET("/api/v1/healthcheck")
    suspend fun healthcheck(): BasicHealthCheckResponse

    @GET("/api/v1/healthcheck/services")
    suspend fun healthcheckServices(): Response<AllProductsHealthCheckResponse>

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