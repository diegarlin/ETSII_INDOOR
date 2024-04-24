package org.altbeacon.apiUsers

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.google.gson.JsonObject
import retrofit2.Response
import android.util.Log

//suspend lo que hace es bloquear el hilo donde se ejecuta para hacer una operación asíncrona sin parar los demás procesos para así
//la aplicación sigua ejecutándose mientras llama a la API
object ApiClientUsuarios {
    private const val BASE_URL = "https://api-flask-t5ze.onrender.com/" // Reemplaza con la URL de tu API

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiServiceUsuarios = retrofit.create(ApiServiceUsuarios::class.java)

    suspend fun login(context: Context, username: String, password: String, deviceID: String): Response<JsonObject> {
        val response = apiService.login(LoginRequest(username, password, deviceID))
        Log.d("api", "Logeo correcto")
        return response
    }

    suspend fun register(context: Context, username: String, password: String, deviceID: String): Response<JsonObject> {
        val response = apiService.register(RegisterRequest(username, password, deviceID))
        Log.d("api", "Registro bien hecho")
        return response
    }
}

