package com.example.solicitudes.data.model

// Estado de la tarjeta de tipo de cambio del menú
data class TipoCambioUiState(
    val cargando: Boolean = true,
    val dolar: Double? = null,         // 1 USD en pesos chilenos
    val euro: Double? = null,          // 1 EUR en pesos chilenos
    val actualizado: String? = null,   // Fecha de la última actualización de la API
    val error: String? = null
)
