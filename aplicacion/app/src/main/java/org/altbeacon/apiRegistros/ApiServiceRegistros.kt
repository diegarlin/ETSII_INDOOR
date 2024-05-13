import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServiceRegistros {
    @GET("/registros")
    suspend fun getRegistros(): Response<JsonObject>

    @POST("/registros")
    suspend fun createRegistro(@Body nuevoRegistro: Registro): Response<JsonObject>


    @GET("/habitaciones/entradas_por_habitacion")
    suspend fun getEntradasPorLetra(
        @Query("letra") letra: String
    ): Response<List<Habitacion>>

    @GET("/habitaciones/entradas_por_habitacion_fecha")
    suspend fun getEntradasPorLetraYFecha(
        @Query("letra") letra: String,
        @Query("fechaInicio") fechaInicio: String,
        @Query("fechaFin") fechaFin: String
    ): Response<List<Habitacion>>

    @GET("/habitaciones/personas_actual_habitaciones")
    suspend fun getPersonasActualPorLetra(
        @Query("letra") letra: String
    ): Response<List<Habitacion>>

    @GET("/habitaciones/personas_actual_fecha")
    suspend fun getPersonasActualPorLetraYFecha(
        @Query("letra") letra: String,
        @Query("fecha") fechaInicio: String
    ): Response<List<Habitacion>>

    data class Habitacion(
        val habitacion: String,
        val numPersonas: Int
    )
}