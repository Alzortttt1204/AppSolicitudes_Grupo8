package com.example.solicitudes.repository

import com.example.solicitudes.data.model.TipoCambio
import com.example.solicitudes.remote.RetrofitInstance
import com.example.solicitudes.remote.TipoCambioApiService

// Este repositorio obtiene el tipo de cambio desde la API externa usando Retrofit
class TipoCambioRepository(
    private val api: TipoCambioApiService = RetrofitInstance.apiTipoCambio
) {

    suspend fun getTipoCambio(): TipoCambio {
        return api.getTipoCambio()
    }
}
