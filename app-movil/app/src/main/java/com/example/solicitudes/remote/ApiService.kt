package com.example.solicitudes.remote

import com.example.solicitudes.data.model.Solicitud
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Esta interfaz define los endpoints HTTP del microservicio ms-solicitudes
interface ApiService {

    // GET: lista todas las solicitudes
    @GET("api/solicitudes")
    suspend fun getSolicitudes(): List<Solicitud>

    // POST: crea una solicitud nueva
    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body solicitud: Solicitud): Solicitud

    // PUT: actualiza la solicitud con ese id
    @PUT("api/solicitudes/{id}")
    suspend fun actualizarSolicitud(@Path("id") id: Long, @Body solicitud: Solicitud): Solicitud

    // DELETE: elimina la solicitud con ese id (el microservicio responde 204 sin contenido)
    @DELETE("api/solicitudes/{id}")
    suspend fun eliminarSolicitud(@Path("id") id: Long): Response<Unit>
}
