import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServiceRegistros {
    @GET("/registros")
    suspend fun getRegistros(): Response<JsonObject>

    @POST("/registros")
    suspend fun createRegistro(@Body nuevoRegistro: Registro): Response<JsonObject>

    @GET("/habitaciones/personas_todas_habitaciones")
    suspend fun getPersonasPorHabitacion(): Response<List<Habitacion>>

    @GET("/habitaciones/personas_por_habitacion/{letra}")
    suspend fun getPersonasPorHabitacionPorLetra(@Path("letra") letra: String): Response<List<Habitacion>>

    @GET("/habitaciones/personas_por_habitacion_fecha")
    suspend fun getPersonasPorHabitacionFecha(
        @Query("habitacion") habitacion: String,
        @Query("fechaInicio") fechaInicio: String,
        @Query("fechaFin") fechaFin: String
    ): Response<List<Habitacion>>

    data class Habitacion(
        val habitacion: String,
        val numPersonas: Int
    )
}