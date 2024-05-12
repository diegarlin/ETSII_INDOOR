package org.altbeacon.apiUsers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.google.gson.JsonObject
import retrofit2.Response
import android.util.Log
import okhttp3.ResponseBody

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

}

