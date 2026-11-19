package com.example.solicitudes.data.model

// Estado del formulario de solicitud
data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,

    // true mientras se espera la respuesta del microservicio
    val guardando: Boolean = false,

    // Mensaje cuando el microservicio guardó o eliminó bien ("Solicitud guardada")
    val mensajeExito: String? = null,

    // Mensaje si no se pudo conectar con el microservicio
    val errorServidor: String? = null,

    // Solicitud que se está editando (null = solicitud nueva)
    val solicitudEditando: Solicitud? = null
)
