package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.data.model.Solicitud
import com.example.solicitudes.data.model.SolicitudUiState
import com.example.solicitudes.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel que mantiene la lista de solicitudes y el estado del formulario
class SolicitudViewModel(
    private val repository: SolicitudRepository = SolicitudRepository()
) : ViewModel() {

    // Categorías del caso
    val categorias = listOf(
        "Desarrollo web",
        "Mantenimiento web",
        "Incidente técnico",
        "Hosting",
        "Soporte de correo",
        "Marketing digital",
        "Solicitud de cambio",
        "Consulta",
        "Acceso o configuración",
        "Otro requerimiento"
    )

    // Lista de solicitudes que viene del microservicio
    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes

    // Mensaje de error de la lista (null = sin error)
    private val _errorLista = MutableStateFlow<String?>(null)
    val errorLista: StateFlow<String?> = _errorLista

    // Estado interno mutable del formulario
    private val _estado = MutableStateFlow(SolicitudUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<SolicitudUiState> = _estado

    // Se llama automáticamente al iniciar
    init {
        cargarSolicitudes()
    }

    // GET: pide la lista al microservicio en segundo plano
    fun cargarSolicitudes() {
        viewModelScope.launch {
            try {
                _solicitudes.value = repository.getSolicitudes()
                _errorLista.value = null
            } catch (e: Exception) {
                _errorLista.value = "No se pudo conectar con el servidor"
            }
        }
    }

    // Actualiza el campo título y limpia su error
    fun onTituloChange(valor: String) {
        _estado.update { it.copy(titulo = valor, tituloError = null) }
    }

    // Actualiza el campo descripción y limpia su error
    fun onDescripcionChange(valor: String) {
        _estado.update { it.copy(descripcion = valor, descripcionError = null) }
    }

    // Actualiza la categoría y limpia su error
    fun onCategoriaChange(valor: String) {
        _estado.update { it.copy(categoria = valor, categoriaError = null) }
    }

    // Actualiza el servicio relacionado y limpia su error
    fun onServicioChange(valor: String) {
        _estado.update { it.copy(servicio = valor, servicioError = null) }
    }

    // Guarda la Uri de la foto (cámara o galería)
    fun onEvidenciaChange(uri: String) {
        _estado.update { it.copy(evidenciaUri = uri) }
    }

    // Deja el formulario vacío para registrar una solicitud nueva
    fun nuevaSolicitud() {
        _estado.value = SolicitudUiState()
    }

    // Carga en el formulario los datos de una solicitud para consultarla o editarla
    fun editarSolicitud(solicitud: Solicitud) {
        _estado.value = SolicitudUiState(
            titulo = solicitud.titulo,
            descripcion = solicitud.descripcion,
            categoria = solicitud.categoria,
            servicio = solicitud.servicio,
            evidenciaUri = solicitud.evidenciaUri,
            solicitudEditando = solicitud
        )
    }

    // REGLAS DEL NEGOCIO
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value

        val tituloError = if (estadoActual.titulo.isBlank()) "El título es obligatorio" else null

        val descripcionError = when {
            estadoActual.descripcion.isBlank() -> "La descripción es obligatoria"
            estadoActual.descripcion.trim().length < MIN_DESCRIPCION ->
                "Debe tener al menos $MIN_DESCRIPCION caracteres"
            else -> null
        }

        val categoriaError = if (estadoActual.categoria.isBlank()) "Selecciona una categoría" else null

        val servicioError = if (estadoActual.servicio.isBlank()) "El servicio relacionado es obligatorio" else null

        _estado.update {
            it.copy(
                tituloError = tituloError,
                descripcionError = descripcionError,
                categoriaError = categoriaError,
                servicioError = servicioError
            )
        }

        return listOfNotNull(tituloError, descripcionError, categoriaError, servicioError).isEmpty()
    }

    // POST si es nueva o PUT si se está editando
    fun guardar() {
        val a = _estado.value
        val datos = Solicitud(
            titulo = a.titulo.trim(),
            descripcion = a.descripcion.trim(),
            categoria = a.categoria,
            servicio = a.servicio.trim(),
            evidenciaUri = a.evidenciaUri
        )
        _estado.update { it.copy(guardando = true, errorServidor = null) }
        viewModelScope.launch {
            try {
                val id = a.solicitudEditando?.id
                if (id == null) {
                    repository.crearSolicitud(datos)
                } else {
                    repository.actualizarSolicitud(id, datos)
                }
                cargarSolicitudes()
                _estado.update { it.copy(guardando = false, mensajeExito = "Solicitud guardada") }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, errorServidor = "No se pudo conectar con el servidor") }
            }
        }
    }

    // DELETE: elimina la solicitud que se está editando
    fun eliminar() {
        val id = _estado.value.solicitudEditando?.id ?: return
        _estado.update { it.copy(guardando = true, errorServidor = null) }
        viewModelScope.launch {
            try {
                if (repository.eliminarSolicitud(id)) {
                    cargarSolicitudes()
                    _estado.update { it.copy(guardando = false, mensajeExito = "Solicitud eliminada") }
                } else {
                    _estado.update { it.copy(guardando = false, errorServidor = "La solicitud ya no existe") }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(guardando = false, errorServidor = "No se pudo conectar con el servidor") }
            }
        }
    }

    companion object {
        const val MIN_DESCRIPCION = 10
    }
}
