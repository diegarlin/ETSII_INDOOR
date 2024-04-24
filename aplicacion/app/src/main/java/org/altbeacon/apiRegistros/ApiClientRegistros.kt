import android.util.Log
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientRegistros {
    private const val BASE_URL = "https://api-mongo-9eqi.onrender.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiServiceRegistros = retrofit.create(ApiServiceRegistros::class.java)

    suspend fun createRegistro(registro: Registro): Response<JsonObject> {
        val response = apiService.createRegistro(registro)
        Log.d("api", "Creación de registro correcto")
        return response
    }

}