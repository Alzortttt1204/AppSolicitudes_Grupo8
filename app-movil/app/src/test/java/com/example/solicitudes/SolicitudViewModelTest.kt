package com.example.solicitudes

import com.example.solicitudes.data.model.Solicitud
import com.example.solicitudes.repository.SolicitudRepository
import com.example.solicitudes.viewmodel.SolicitudViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // Datos simulados (como si vinieran del microservicio)
    val fakeSolicitudes = listOf(
        Solicitud(
            id = 1,
            codigo = "SOL-AAAA1111",
            titulo = "Sitio caído",
            descripcion = "El sitio web no carga desde ayer",
            categoria = "Incidente técnico",
            servicio = "Sitio web",
            estado = "Recibida",
            fechaHora = "17/11/2026 10:00"
        ),
        Solicitud(
            id = 2,
            codigo = "SOL-BBBB2222",
            titulo = "Nuevo correo",
            descripcion = "Crear una cuenta de correo nueva",
            categoria = "Soporte de correo",
            servicio = "Correo",
            estado = "Recibida",
            fechaHora = "17/11/2026 11:00"
        )
    )

    // Crea un repositorio simulado que devuelve la lista de prueba
    fun crearFakeRepository(): SolicitudRepository {
        val fakeRepository = mockk<SolicitudRepository>()
        coEvery { fakeRepository.getSolicitudes() } returns fakeSolicitudes
        return fakeRepository
    }

    // Llena el formulario con datos válidos
    fun llenarFormulario(viewModel: SolicitudViewModel) {
        viewModel.onTituloChange("Sitio caído")
        viewModel.onDescripcionChange("El sitio web no carga desde ayer")
        viewModel.onCategoriaChange("Incidente técnico")
        viewModel.onServicioChange("Sitio web")
    }

    "solicitudes debe contener los datos del microservicio al iniciar" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())

            advanceUntilIdle()

            testViewModel.solicitudes.value shouldContainExactly fakeSolicitudes
            testViewModel.errorLista.value.shouldBeNull()
        }
    }

    "errorLista debe tener un mensaje si el microservicio no responde" {
        runTest(dispatcher) {
            val fakeRepository = mockk<SolicitudRepository>()
            coEvery { fakeRepository.getSolicitudes() } throws RuntimeException("Sin conexión")

            val testViewModel = SolicitudViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.solicitudes.value shouldBe emptyList()
            testViewModel.errorLista.value shouldBe "No se pudo conectar con el servidor"
        }
    }

    "validarFormulario con campos vacíos debe devolver false y marcar los 4 errores" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())

            testViewModel.validarFormulario() shouldBe false

            val estado = testViewModel.estado.value
            estado.tituloError shouldBe "El título es obligatorio"
            estado.descripcionError shouldBe "La descripción es obligatoria"
            estado.categoriaError shouldBe "Selecciona una categoría"
            estado.servicioError shouldBe "El servicio relacionado es obligatorio"
        }
    }

    "validarFormulario debe rechazar una descripción de menos de 10 caracteres" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())
            llenarFormulario(testViewModel)
            testViewModel.onDescripcionChange("Corta")

            testViewModel.validarFormulario() shouldBe false
            testViewModel.estado.value.descripcionError shouldBe "Debe tener al menos 10 caracteres"
        }
    }

    "validarFormulario con datos correctos debe devolver true y sin errores" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())
            llenarFormulario(testViewModel)

            testViewModel.validarFormulario() shouldBe true
            testViewModel.estado.value.tituloError.shouldBeNull()
            testViewModel.estado.value.descripcionError.shouldBeNull()
        }
    }

    "escribir en un campo debe borrar su error" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())
            testViewModel.validarFormulario()

            testViewModel.onTituloChange("Nuevo título")
            testViewModel.onCategoriaChange("Hosting")

            testViewModel.estado.value.titulo shouldBe "Nuevo título"
            testViewModel.estado.value.tituloError.shouldBeNull()
            testViewModel.estado.value.categoriaError.shouldBeNull()
        }
    }

    "guardar una solicitud nueva debe llamar a crearSolicitud (POST)" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.crearSolicitud(any()) } returns fakeSolicitudes[0]

            val testViewModel = SolicitudViewModel(fakeRepository)
            llenarFormulario(testViewModel)
            testViewModel.onEvidenciaChange("content://foto/1")
            testViewModel.guardar()
            advanceUntilIdle()

            coVerify {
                fakeRepository.crearSolicitud(
                    Solicitud(
                        titulo = "Sitio caído",
                        descripcion = "El sitio web no carga desde ayer",
                        categoria = "Incidente técnico",
                        servicio = "Sitio web",
                        evidenciaUri = "content://foto/1"
                    )
                )
            }
            testViewModel.estado.value.mensajeExito shouldBe "Solicitud guardada"
            testViewModel.estado.value.guardando shouldBe false
        }
    }

    "guardar una solicitud que se está editando debe llamar a actualizarSolicitud con su id (PUT)" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.actualizarSolicitud(any(), any()) } returns fakeSolicitudes[0]

            val testViewModel = SolicitudViewModel(fakeRepository)
            testViewModel.editarSolicitud(fakeSolicitudes[0])
            testViewModel.onTituloChange("Sitio caído (editado)")
            testViewModel.guardar()
            advanceUntilIdle()

            coVerify { fakeRepository.actualizarSolicitud(1, match { it.titulo == "Sitio caído (editado)" }) }
            coVerify(exactly = 0) { fakeRepository.crearSolicitud(any()) }
            testViewModel.estado.value.mensajeExito shouldBe "Solicitud guardada"
        }
    }

    "guardar con el microservicio caído debe mostrar errorServidor" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.crearSolicitud(any()) } throws RuntimeException("Sin conexión")

            val testViewModel = SolicitudViewModel(fakeRepository)
            llenarFormulario(testViewModel)
            testViewModel.guardar()
            advanceUntilIdle()

            testViewModel.estado.value.errorServidor shouldBe "No se pudo conectar con el servidor"
            testViewModel.estado.value.mensajeExito.shouldBeNull()
            testViewModel.estado.value.guardando shouldBe false
        }
    }

    "eliminar debe llamar a eliminarSolicitud con el id (DELETE)" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.eliminarSolicitud(2) } returns true

            val testViewModel = SolicitudViewModel(fakeRepository)
            testViewModel.editarSolicitud(fakeSolicitudes[1])
            testViewModel.eliminar()
            advanceUntilIdle()

            coVerify { fakeRepository.eliminarSolicitud(2) }
            testViewModel.estado.value.mensajeExito shouldBe "Solicitud eliminada"
        }
    }

    "eliminar una solicitud que ya no existe debe mostrar un error" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.eliminarSolicitud(2) } returns false

            val testViewModel = SolicitudViewModel(fakeRepository)
            testViewModel.editarSolicitud(fakeSolicitudes[1])
            testViewModel.eliminar()
            advanceUntilIdle()

            testViewModel.estado.value.errorServidor shouldBe "La solicitud ya no existe"
        }
    }

    "eliminar con el microservicio caído debe mostrar errorServidor" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.eliminarSolicitud(any()) } throws RuntimeException("Sin conexión")

            val testViewModel = SolicitudViewModel(fakeRepository)
            testViewModel.editarSolicitud(fakeSolicitudes[1])
            testViewModel.eliminar()
            advanceUntilIdle()

            testViewModel.estado.value.errorServidor shouldBe "No se pudo conectar con el servidor"
        }
    }

    "eliminar sin una solicitud seleccionada no debe llamar al microservicio" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()

            val testViewModel = SolicitudViewModel(fakeRepository)
            testViewModel.eliminar()
            advanceUntilIdle()

            coVerify(exactly = 0) { fakeRepository.eliminarSolicitud(any()) }
        }
    }

    "editarSolicitud debe llenar el formulario y nuevaSolicitud debe vaciarlo" {
        runTest(dispatcher) {
            val testViewModel = SolicitudViewModel(crearFakeRepository())

            testViewModel.editarSolicitud(fakeSolicitudes[0])
            testViewModel.estado.value.titulo shouldBe "Sitio caído"
            testViewModel.estado.value.solicitudEditando.shouldNotBeNull()

            testViewModel.nuevaSolicitud()
            testViewModel.estado.value.titulo shouldBe ""
            testViewModel.estado.value.solicitudEditando.shouldBeNull()
        }
    }
})
