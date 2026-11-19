package com.example.solicitudes.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton que contiene la configuración de Retrofit
object RetrofitInstance {

    // Microservicio propio (Spring Boot). 10.0.2.2 es el "localhost" del computador visto desde el emulador
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // URL base del microservicio
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(ApiService::class.java) // Implementa la interfaz ApiService
    }

    // API externa pública (tipo de cambio del dólar). Es otro servidor, por eso otra URL base
    val apiTipoCambio: TipoCambioApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/") // URL base de la API externa
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TipoCambioApiService::class.java)
    }
}
