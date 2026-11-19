package com.example.solicitudes.data.model

import com.google.gson.annotations.SerializedName

// Esta clase representa la respuesta de la API externa open.er-api.com
// Ejemplo: { "result": "success", "time_last_update_utc": "...", "rates": { "CLP": 950.1, "EUR": 0.86 } }
data class TipoCambio(
    val result: String,

    // @SerializedName: el nombre en el JSON es distinto al nombre en Kotlin
    @SerializedName("time_last_update_utc")
    val ultimaActualizacion: String,

    // Cuánto vale 1 dólar en cada moneda
    val rates: Map<String, Double>
)
