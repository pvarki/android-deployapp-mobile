package com.pvarki.deployapp.data.api

import com.pvarki.deployapp.data.model.AllProductsHealthCheckResponse
import com.pvarki.deployapp.data.model.AllProductsInstructionFiles
import com.pvarki.deployapp.data.model.BasicHealthCheckResponse
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.model.EnrollResponse
import com.pvarki.deployapp.data.model.EnrollmentAcceptIn
import com.pvarki.deployapp.data.model.EnrollmentDemoteIn
import com.pvarki.deployapp.data.model.EnrollmentGenVerifiOut
import com.pvarki.deployapp.data.model.EnrollmentHaveIBeenAcceptedOut
import com.pvarki.deployapp.data.model.EnrollmentInitIn
import com.pvarki.deployapp.data.model.EnrollmentInitOut
import com.pvarki.deployapp.data.model.EnrollmentInviteCodeActivateIn
import com.pvarki.deployapp.data.model.EnrollmentInviteCodeCreateOut
import com.pvarki.deployapp.data.model.EnrollmentInviteCodeDeactivateIn
import com.pvarki.deployapp.data.model.EnrollmentIsInvitecodeActiveOut
import com.pvarki.deployapp.data.model.EnrollmentListOut
import com.pvarki.deployapp.data.model.EnrollmentLockIn
import com.pvarki.deployapp.data.model.EnrollmentPoolListOut
import com.pvarki.deployapp.data.model.EnrollmentPromoteIn
import com.pvarki.deployapp.data.model.EnrollmentShowVerificationCodeOut
import com.pvarki.deployapp.data.model.EnrollmentStatusOut
import com.pvarki.deployapp.data.model.LoginCodeRequest
import com.pvarki.deployapp.data.model.MLTSorJwtPayload
import com.pvarki.deployapp.data.model.MyResponse
import com.pvarki.deployapp.data.model.OperationResultResponse
import com.pvarki.deployapp.data.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Get the signed cert in PEM format (no keys)
    @GET("/api/v1/enduserpfx/{callsign}_{server}.pem")
    suspend fun signedChainPEMFile(@Path("callsign") callSign: String, @Path("server") serverName: String): Response<ResponseBody>

    @GET("/api/v1/enduserpfx/{callsign}_{server}.pfx")
    suspend fun signedChainPFXFile(@Path("callsign") callSign: String, @Path("server") serverName: String): Response<ResponseBody>


    @GET("/api/v1/enduserpfx/{callsign}.pem")
    suspend fun getUserPem(@Path("callsign") callSign: String): Response<ResponseBody>

    @GET("/api/v1/check-auth/mtls")
    suspend fun returnMtlsPayload() : Response<ResponseBody>

    @GET("/api/v1/enduserpfx/{callsign}.pfx")
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

    @GET("/api/v1/enrollment/pools")
    suspend fun listPools(@Query("owner_cs") ownerCs: String? = null): EnrollmentPoolListOut

    @POST("/api/v1/enrollment/generate-verification-code")
    suspend fun postGenerateVerificationCode(): EnrollmentGenVerifiOut

    @GET("/api/v1/enrollment/show-verification-code-info")
    suspend fun requestShowVerificationCode(@Query("verification_code") verificationCode: String): EnrollmentShowVerificationCodeOut

    @GET("/api/v1/enrollment/have-i-been-accepted")
    suspend fun requestHaveIBeenAccepted(): EnrollmentHaveIBeenAcceptedOut

    @GET("/api/v1/enrollment/list")
    suspend fun requestEnrollmentList(@Query("code") code: String? = null): EnrollmentListOut

    @POST("/api/v1/enrollment/init")
    suspend fun requestEnrollmentInit(@Body enrollment: EnrollmentInitIn): EnrollmentInitOut

    @POST("/api/v1/enrollment/promote")
    suspend fun requestEnrollmentPromote(@Body promotion: EnrollmentPromoteIn): OperationResultResponse

    @POST("/api/v1/enrollment/demote")
    suspend fun requestEnrollmentDemote(@Body demotion: EnrollmentDemoteIn): OperationResultResponse

    @POST("/api/v1/enrollment/lock")
    suspend fun requestEnrollmentLock(@Body lock: EnrollmentLockIn): OperationResultResponse

    @POST("/api/v1/enrollment/accept")
    suspend fun postEnrollmentAccept(@Body accept: EnrollmentAcceptIn): OperationResultResponse

    @POST("/api/v1/enrollment/invitecode/create")
    suspend fun postInviteCode(): EnrollmentInviteCodeCreateOut

    @PUT("/api/v1/enrollment/invitecode/activate")
    suspend fun putActivateInviteCode(@Body activate: EnrollmentInviteCodeActivateIn): OperationResultResponse

    @PUT("/api/v1/enrollment/invitecode/deactivate")
    suspend fun putDeactivateInviteCode(@Body deactivate: EnrollmentInviteCodeDeactivateIn): OperationResultResponse

    @DELETE("/api/v1/enrollment/invitecode/{invite_code}")
    suspend fun deleteInviteCode(@Path("invite_code") inviteCode: String): OperationResultResponse

    @GET("/api/v1/enrollment/invitecode")
    suspend fun getInviteCodes(@Query("invitecode") invitecode: String): EnrollmentIsInvitecodeActiveOut

    @GET("endpoint/{id}")
    fun getData(@Path("id") id: String): Call<MyResponse>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>
}
