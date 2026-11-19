package com.example.solicitudes.data.model

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",

    val usuarioError: String? = null,
    val claveError: String? = null
)
