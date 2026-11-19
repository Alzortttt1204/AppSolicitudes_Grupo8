package com.example.solicitudes.remote

import com.example.solicitudes.data.model.TipoCambio
import retrofit2.http.GET

// Esta interfaz define el endpoint de la API externa (no es nuestra, solo la consumimos)
interface TipoCambioApiService {

    // GET https://open.er-api.com/v6/latest/USD -> valor de 1 dólar en todas las monedas
    @GET("v6/latest/USD")
    suspend fun getTipoCambio(): TipoCambio
}
