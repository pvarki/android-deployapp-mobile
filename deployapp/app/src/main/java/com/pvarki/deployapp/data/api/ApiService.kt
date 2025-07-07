package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.data.model.AllProductsHealthCheckResponse
import com.pvarki.deployapp.data.model.AllProductsInstructionFiles
import com.pvarki.deployapp.data.model.BasicHealthCheckResponse
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.model.EnrollResponse
import com.pvarki.deployapp.data.model.EnrollmentStatusOut
import com.pvarki.deployapp.data.model.LoginCodeRequest
import com.pvarki.deployapp.data.model.MLTSorJwtPayload
import com.pvarki.deployapp.data.model.MyResponse
import com.pvarki.deployapp.data.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {


    @GET("/api/v1/check-auth/mtls")
    suspend fun returnMtlsPayload() : Response<ResponseBody>

    @GET("/api/v1/enduserpfx/{callsign}")
    suspend fun getUserPfx(@Path("callsign") callSign: String): Response<ResponseBody>

    @GET("/api/v1/enrollment/status")
    suspend fun requestEnrollmentStatus(@Query("callsign") callSign: String): EnrollmentStatusOut

    @GET("/api/v1/instructions/user")
    suspend fun userInstructionFragment(): AllProductsInstructionFiles

    @GET("/api/v1/check-auth/validuser")
    suspend fun returnValiduserPayload(): MLTSorJwtPayload

    @GET("/api/v1/check-auth/jwt")
    suspend fun returnJwtPayload(): Response<ResponseBody>

    @GET("/api/v1/token/jwt/refresh")
    suspend fun refreshToken(): Response<ResponseBody>

    @POST("/api/v1/token/code/exchange")
    suspend fun exchangeCode(@Body loginCodeRequest: LoginCodeRequest): Response<ResponseBody>

    @POST("/api/v1/token/jwt/exchange")
    suspend fun exchangeToken(): Response<ResponseBody>

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


}