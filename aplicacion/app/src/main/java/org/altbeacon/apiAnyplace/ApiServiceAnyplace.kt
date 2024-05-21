import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiServiceAnyplace {
    @Headers("Content-Type: application/json",
        "access_token: apGoogle_wyeF4kjtzt4565tUEZl1nRLflBpx4KfN5XKgpAaR0L18YePMZvzX3zCwSXT5QQAaTEsI5OD5p96mcAUAAgmWCqrjKcx77BTOcH6tCu9eIvzWtaeOr5ae1jlT8IedJw6miNsJvQUql5o9uOETekTpQ4DX04Svb6kTvacLkh7ym8mcIno7LvOf0cU2EWHvit8Y64MfbUrsnBB6zSHhGSZkageVvM3z9nxc4OlYo8tf9vu5MI2sMso77u27e5QTJPg35ZYMMh5MB171KGMxLSxoCg9hdK91AaHhS1JIXVQc9ZigHuqtH4Dm4vVNJfua5H0rewQOqmh7i9dj2sMTKKWdKTDy64DAh64Z9hHD0O0R9CC9fQxgcHGgq5e0j2RQyfLJOeoaiZY3ycKqFl0xcYKznsYWmBm0fBzhW1El6ySAKpLrcDjbO5wkcCOMgUxZcHq2ZBGNF0Ygu8CEsXgJUytXyS9V2ICXSrBgxx8B6LTneT7B0leYkPF2ap")
    @POST("/api/mapping/pois/search")
    fun searchPoi(@Body searchPoiData: Map<String, String>): Call<Map<String, Any>>

    @Headers("Content-Type: application/json",
        "access_token: apGoogle_wyeF4kjtzt4565tUEZl1nRLflBpx4KfN5XKgpAaR0L18YePMZvzX3zCwSXT5QQAaTEsI5OD5p96mcAUAAgmWCqrjKcx77BTOcH6tCu9eIvzWtaeOr5ae1jlT8IedJw6miNsJvQUql5o9uOETekTpQ4DX04Svb6kTvacLkh7ym8mcIno7LvOf0cU2EWHvit8Y64MfbUrsnBB6zSHhGSZkageVvM3z9nxc4OlYo8tf9vu5MI2sMso77u27e5QTJPg35ZYMMh5MB171KGMxLSxoCg9hdK91AaHhS1JIXVQc9ZigHuqtH4Dm4vVNJfua5H0rewQOqmh7i9dj2sMTKKWdKTDy64DAh64Z9hHD0O0R9CC9fQxgcHGgq5e0j2RQyfLJOeoaiZY3ycKqFl0xcYKznsYWmBm0fBzhW1El6ySAKpLrcDjbO5wkcCOMgUxZcHq2ZBGNF0Ygu8CEsXgJUytXyS9V2ICXSrBgxx8B6LTneT7B0leYkPF2ap")
    @POST("/api/auth/mapping/pois/update")
    fun updatePoi(@Body updatePoiData: Map<String, String>): Call<Map<String, Any>>
}