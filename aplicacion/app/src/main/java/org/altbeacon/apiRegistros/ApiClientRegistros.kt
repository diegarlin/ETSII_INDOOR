import ApiServiceRegistros.Habitacion
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


    suspend fun getEntradasPorLetra(letra: String): Response<List<Habitacion>> {
        val response = apiService.getEntradasPorLetra(letra)
        Log.d("api", "Obtención de entradas por letra correcto")
        return response
    }

    suspend fun getEntradasPorLetraYFecha(
        letra: String,
        fechaInicio: String,
        fechaFin: String
    ): Response<List<Habitacion>> {
        val response = apiService.getEntradasPorLetraYFecha(letra, fechaInicio, fechaFin)
        Log.d("api", "Obtención de entradas por habitación y fecha correcto")
        return response
    }

    suspend fun getPersonasActualPorLetra(letra: String): Response<List<Habitacion>> {
        val response = apiService.getPersonasActualPorLetra(letra)
        Log.d("api", "Obtención de personas actual por letra correcto")
        return response
    }

    suspend fun getPersonasActualPorLetraYFecha(
        letra: String,
        fecha: String
    ): Response<List<Habitacion>> {
        val response = apiService.getPersonasActualPorLetraYFecha(letra, fecha)
        Log.d("api", "Obtención de entradas por habitación y fecha correcto")
        return response
    }
}