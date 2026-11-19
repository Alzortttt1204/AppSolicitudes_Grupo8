package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.data.model.TipoCambioUiState
import com.example.solicitudes.repository.TipoCambioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel que mantiene el tipo de cambio obtenido de la API externa
class TipoCambioViewModel(
    private val repository: TipoCambioRepository = TipoCambioRepository()
) : ViewModel() {

    // Estado interno mutable
    private val _estado = MutableStateFlow(TipoCambioUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<TipoCambioUiState> = _estado

    // Se llama automáticamente al iniciar
    init {
        cargarTipoCambio()
    }

    // Obtiene los datos en segundo plano
    fun cargarTipoCambio() {
        _estado.value = TipoCambioUiState(cargando = true)
        viewModelScope.launch {
            try {
                val respuesta = repository.getTipoCambio()
                val clp = respuesta.rates["CLP"]
                val eur = respuesta.rates["EUR"]
                if (clp == null) {
                    _estado.value = TipoCambioUiState(cargando = false, error = "La API no trae el peso chileno")
                } else {
                    _estado.value = TipoCambioUiState(
                        cargando = false,
                        dolar = clp,
                        // La API da 1 USD en euros; 1 EUR en pesos = pesos por dólar / euros por dólar
                        euro = if (eur != null && eur > 0) clp / eur else null,
                        actualizado = respuesta.ultimaActualizacion
                    )
                }
            } catch (e: Exception) {
                _estado.value = TipoCambioUiState(cargando = false, error = "No se pudo obtener el tipo de cambio")
            }
        }
    }
}
