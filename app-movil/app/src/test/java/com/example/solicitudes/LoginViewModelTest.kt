package com.example.solicitudes

import com.example.solicitudes.viewmodel.LoginViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// El login no usa corrutinas ni repositorio: solo se prueban sus validaciones
class LoginViewModelTest : StringSpec({

    "validarFormulario con campos vacíos debe devolver false y marcar ambos errores" {
        val testViewModel = LoginViewModel()

        testViewModel.validarFormulario() shouldBe false
        testViewModel.estado.value.usuarioError shouldBe "Campo obligatorio"
        testViewModel.estado.value.claveError shouldBe "Campo obligatorio"
    }

    "validarFormulario con clave incorrecta debe devolver false" {
        val testViewModel = LoginViewModel()
        testViewModel.onUsuarioChange("demo")
        testViewModel.onClaveChange("9999")

        testViewModel.validarFormulario() shouldBe false
        testViewModel.estado.value.claveError shouldBe "Usuario o clave incorrectos"
    }

    "validarFormulario con el usuario de prueba debe devolver true" {
        val testViewModel = LoginViewModel()
        testViewModel.onUsuarioChange("demo")
        testViewModel.onClaveChange("1234")

        testViewModel.validarFormulario() shouldBe true
        testViewModel.estado.value.usuarioError.shouldBeNull()
        testViewModel.estado.value.claveError.shouldBeNull()
    }

    "escribir en un campo debe borrar su error" {
        val testViewModel = LoginViewModel()
        testViewModel.validarFormulario()

        testViewModel.onUsuarioChange("demo")
        testViewModel.onClaveChange("1")

        testViewModel.estado.value.usuarioError.shouldBeNull()
        testViewModel.estado.value.claveError.shouldBeNull()
    }
})
