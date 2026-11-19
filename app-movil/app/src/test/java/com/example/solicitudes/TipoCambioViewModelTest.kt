package com.example.solicitudes

import com.example.solicitudes.data.model.TipoCambio
import com.example.solicitudes.repository.TipoCambioRepository
import com.example.solicitudes.viewmodel.TipoCambioViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

import io.mockk.coEvery
import io.mockk.mockk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TipoCambioViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    "estado debe tener el dólar y el euro en pesos después de cargar" {
        runTest(dispatcher) {
            // Respuesta simulada de la API externa: 1 USD = 950 CLP y 1 USD = 0.5 EUR
            val fakeRepository = mockk<TipoCambioRepository>()
            coEvery { fakeRepository.getTipoCambio() } returns TipoCambio(
                result = "success",
                ultimaActualizacion = "Tue, 17 Nov 2026 00:02:31 +0000",
                rates = mapOf("CLP" to 950.0, "EUR" to 0.5)
            )

            val testViewModel = TipoCambioViewModel(fakeRepository)
            advanceUntilIdle()

            val estado = testViewModel.estado.value
            estado.cargando shouldBe false
            estado.dolar shouldBe 950.0
            estado.euro shouldBe 1900.0 // 950 / 0.5
            estado.actualizado shouldBe "Tue, 17 Nov 2026 00:02:31 +0000"
            estado.error.shouldBeNull()
        }
    }

    "si la API no trae el euro, solo se muestra el dólar" {
        runTest(dispatcher) {
            val fakeRepository = mockk<TipoCambioRepository>()
            coEvery { fakeRepository.getTipoCambio() } returns TipoCambio(
                result = "success",
                ultimaActualizacion = "hoy",
                rates = mapOf("CLP" to 950.0)
            )

            val testViewModel = TipoCambioViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.estado.value.dolar shouldBe 950.0
            testViewModel.estado.value.euro.shouldBeNull()
        }
    }

    "si la API no trae el peso chileno, debe mostrar un error" {
        runTest(dispatcher) {
            val fakeRepository = mockk<TipoCambioRepository>()
            coEvery { fakeRepository.getTipoCambio() } returns TipoCambio(
                result = "success",
                ultimaActualizacion = "hoy",
                rates = mapOf("EUR" to 0.5)
            )

            val testViewModel = TipoCambioViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.estado.value.error shouldBe "La API no trae el peso chileno"
            testViewModel.estado.value.dolar.shouldBeNull()
        }
    }

    "si la API externa falla, debe mostrar un error y se puede reintentar" {
        runTest(dispatcher) {
            val fakeRepository = mockk<TipoCambioRepository>()
            coEvery { fakeRepository.getTipoCambio() } throws RuntimeException("Sin internet")

            val testViewModel = TipoCambioViewModel(fakeRepository)
            advanceUntilIdle()
            testViewModel.estado.value.error shouldBe "No se pudo obtener el tipo de cambio"

            // Reintentar cuando la API vuelve a responder
            coEvery { fakeRepository.getTipoCambio() } returns TipoCambio(
                result = "success",
                ultimaActualizacion = "hoy",
                rates = mapOf("CLP" to 900.0, "EUR" to 0.9)
            )
            testViewModel.cargarTipoCambio()
            advanceUntilIdle()

            testViewModel.estado.value.error.shouldBeNull()
            testViewModel.estado.value.dolar shouldBe 900.0
        }
    }
})
