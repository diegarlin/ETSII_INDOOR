import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response

interface ApiServiceRegistros {
    @GET("/registros")
    suspend fun getRegistros(): Response<JsonObject>

    @POST("/registros")
    suspend fun createRegistro(@Body nuevoRegistro: Registro): Response<JsonObject>
}