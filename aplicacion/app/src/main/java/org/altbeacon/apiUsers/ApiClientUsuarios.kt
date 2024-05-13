package org.altbeacon.apiUsers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.google.gson.JsonObject
import retrofit2.Response
import android.util.Log
import okhttp3.ResponseBody
import org.altbeacon.utils.SharedPreferencesManager

//suspend lo que hace es bloquear el hilo donde se ejecuta para hacer una operación asíncrona sin parar los demás procesos para así
//la aplicación sigua ejecutándose mientras llama a la API
object ApiClientUsuarios {
    private const val BASE_URL = "https://api-flask-t5ze.onrender.com/" // Reemplaza con la URL de tu API

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiServiceUsuarios = retrofit.create(ApiServiceUsuarios::class.java)

    suspend fun login(context: Context, usernameOrEmail: String, password: String, deviceID: String): Response<JsonObject> {
        val response = apiService.login(LoginRequest(usernameOrEmail, password, deviceID))
        Log.d("api", "Logeo correcto")
        return response
    }

    suspend fun register(context: Context, username: String, email: String, password: String, deviceID: String): Response<JsonObject> {
        val response = apiService.register(RegisterRequest(username, email, password, deviceID))
        Log.d("api", "Registro bien hecho")
        return response
    }

    suspend fun cerrar_entradas(context: Context, token: String): Response<JsonObject> {

        val response = apiService.cerrar_entradas("Bearer $token")
        Log.d("api", "Registro de salidas realizado con éxito")
        Log.d("api", response.toString())

        return response
    }

    suspend fun send_email(context: Context, subject: String, body: String, token: String): Response<JsonObject> {
        val request = EmailRequest(subject, body)
        val response = apiService.send_email("Bearer $token", request)

        Log.d("api", response.toString())

        return response
    }

    suspend fun getUsers(context: Context): Response<List<User>> {
        val token = SharedPreferencesManager.getTokenFromSharedPreferences(context)
        if (token != null) {
            val response = apiService.getUsers("Bearer $token")
            Log.d("api", response.toString())
            return response
        }else{
            throw Exception("No se encontró el token. Logueate de nuevo o por primera vez")

        }
    }

    suspend fun getUser(context: Context, userId: Int): Response<User> {
        val token = SharedPreferencesManager.getTokenFromSharedPreferences(context)
        if (token != null) {
            return apiService.getUser("Bearer $token",userId)
        } else {
            throw Exception("No se encontró el token. Logueate de nuevo o por primera vez")
        }
    }

    suspend fun updateUser(context: Context, userId: Int, request: UpdateUserRequest): Response<User> {
        val token = SharedPreferencesManager.getTokenFromSharedPreferences(context)
        if (token != null) {
            return apiService.updateUser("Bearer $token", userId, request)
        } else {
            throw Exception("No se encontró el token. Logueate de nuevo o por primera vez")
        }
    }

}

