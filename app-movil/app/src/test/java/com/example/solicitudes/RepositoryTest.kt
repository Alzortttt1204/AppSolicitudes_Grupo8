package com.example.solicitudes

import com.example.solicitudes.data.model.Solicitud
import com.example.solicitudes.data.model.TipoCambio
import com.example.solicitudes.remote.ApiService
import com.example.solicitudes.remote.TipoCambioApiService
import com.example.solicitudes.repository.SolicitudRepository
import com.example.solicitudes.repository.TipoCambioRepository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

import retrofit2.Response

// Prueba que los repositorios llamen al endpoint correcto. La API de Retrofit se simula con MockK
class RepositoryTest : StringSpec({

    val solicitud = Solicitud(
        id = 1,
        codigo = "SOL-AAAA1111",
        titulo = "Sitio caído",
        descripcion = "El sitio web no carga desde ayer",
        categoria = "Incidente técnico",
        servicio = "Sitio web"
    )

    "getSolicitudes debe devolver lo que entrega el microservicio (GET)" {
        val fakeApi = mockk<ApiService>()
        coEvery { fakeApi.getSolicitudes() } returns listOf(solicitud)

        SolicitudRepository(fakeApi).getSolicitudes() shouldContainExactly listOf(solicitud)
    }

    "crearSolicitud debe enviar la solicitud al microservicio (POST)" {
        val fakeApi = mockk<ApiService>()
        coEvery { fakeApi.crearSolicitud(solicitud) } returns solicitud

        SolicitudRepository(fakeApi).crearSolicitud(solicitud) shouldBe solicitud
        coVerify { fakeApi.crearSolicitud(solicitud) }
    }

    "actualizarSolicitud debe enviar el id y los datos (PUT)" {
        val fakeApi = mockk<ApiService>()
        coEvery { fakeApi.actualizarSolicitud(1, solicitud) } returns solicitud

        SolicitudRepository(fakeApi).actualizarSolicitud(1, solicitud) shouldBe solicitud
        coVerify { fakeApi.actualizarSolicitud(1, solicitud) }
    }

    "eliminarSolicitud debe devolver true si el microservicio responde 204 (DELETE)" {
        // Respuesta HTTP simulada: isSuccessful = true (código 2xx)
        val respuestaOk = mockk<Response<Unit>>()
        every { respuestaOk.isSuccessful } returns true

        val fakeApi = mockk<ApiService>()
        coEvery { fakeApi.eliminarSolicitud(1) } returns respuestaOk

        SolicitudRepository(fakeApi).eliminarSolicitud(1) shouldBe true
    }

    "eliminarSolicitud debe devolver false si el microservicio responde 404" {
        // Respuesta HTTP simulada: isSuccessful = false (código 404)
        val respuesta404 = mockk<Response<Unit>>()
        every { respuesta404.isSuccessful } returns false

        val fakeApi = mockk<ApiService>()
        coEvery { fakeApi.eliminarSolicitud(99) } returns respuesta404

        SolicitudRepository(fakeApi).eliminarSolicitud(99) shouldBe false
    }

    "RetrofitInstance debe crear ambas APIs sin errores (URLs base válidas)" {
        // Si una URL base estuviera mal escrita, Retrofit lanzaría un error al crearse
        SolicitudRepository()
        TipoCambioRepository()
    }

    "getTipoCambio debe devolver lo que entrega la API externa" {
        val fakeApi = mockk<TipoCambioApiService>()
        val respuesta = TipoCambio("success", "hoy", mapOf("CLP" to 950.0))
        coEvery { fakeApi.getTipoCambio() } returns respuesta

        TipoCambioRepository(fakeApi).getTipoCambio() shouldBe respuesta
    }
})
