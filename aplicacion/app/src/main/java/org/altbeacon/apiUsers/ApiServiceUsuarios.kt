package org.altbeacon.apiUsers

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServiceUsuarios {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<JsonObject>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<JsonObject>

    @GET("/cerrar_entradas")
    suspend fun cerrar_entradas(@Header("Authorization") token: String): Response<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("/send_email")
    suspend fun send_email(@Header("Authorization") token: String, @Body request: EmailRequest): Response<JsonObject>

    @GET("/users")
    suspend fun getUsers(): Response<List<User>>

    @GET("/users/{user_id}")
    suspend fun getUser(@Header("Authorization") token: String, @Path("user_id") userId: Int): Response<User>

    @PUT("/users/{user_id}")
    suspend fun updateUser(@Header("Authorization") token: String, @Path("user_id") userId: Int, @Body request: UpdateUserRequest): Response<User>

}
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String,
    val deviceID: String
)

data class RegisterRequest(val username: String,
                           val email: String,
                           val password: String,
                           val deviceID: String)
data class EmailRequest(val subject: String,
                        val body: String)
data class UpdateUserRequest(val username: String,
                             val email: String,
                             val admin: Boolean,
                             val profesor: Boolean,
                             val despacho: String)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val admin: Boolean,
    val profesor: Boolean,
    val despacho: String,
    val deviceID: String
)
