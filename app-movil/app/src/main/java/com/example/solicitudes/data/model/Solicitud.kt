package com.example.solicitudes.data.model

// Esta clase representa una solicitud que se envía y recibe del microservicio (JSON)
data class Solicitud(
    val id: Long? = null,             // Lo genera la base de datos del microservicio
    val codigo: String? = null,       // SOL-XXXXXXXX, lo genera el microservicio
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    val estado: String? = null,       // "Recibida" al crear, lo pone el microservicio
    val fechaHora: String? = null,    // Fecha de registro, la pone el microservicio
    val evidenciaUri: String? = null  // Foto de evidencia (cámara o galería)
)
