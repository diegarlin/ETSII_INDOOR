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

    @GET("/habitaciones/personas_por_habitacion")
    suspend fun getPersonasPorHabitacion(): Response<List<Habitacion>>

    data class Habitacion(
        val habitacion: String,
        val numPersonas: Int
    )
}