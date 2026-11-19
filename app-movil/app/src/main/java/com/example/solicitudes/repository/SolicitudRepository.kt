package com.example.solicitudes.repository

import com.example.solicitudes.data.model.Solicitud
import com.example.solicitudes.remote.ApiService
import com.example.solicitudes.remote.RetrofitInstance

// Este repositorio se encarga de acceder a los datos del microservicio usando Retrofit
class SolicitudRepository(
    private val api: ApiService = RetrofitInstance.api
) {

    suspend fun getSolicitudes(): List<Solicitud> {
        return api.getSolicitudes()
    }

    suspend fun crearSolicitud(solicitud: Solicitud): Solicitud {
        return api.crearSolicitud(solicitud)
    }

    suspend fun actualizarSolicitud(id: Long, solicitud: Solicitud): Solicitud {
        return api.actualizarSolicitud(id, solicitud)
    }

    // Devuelve true si el microservicio respondió OK (204)
    suspend fun eliminarSolicitud(id: Long): Boolean {
        return api.eliminarSolicitud(id).isSuccessful
    }
}
