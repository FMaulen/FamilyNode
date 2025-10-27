package com.pointer.familynode.viewmodel

import androidx.lifecycle.ViewModel
import com.pointer.familynode.model.UserErrors
import com.pointer.familynode.model.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SignUpViewModel: ViewModel() {


    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = nombre,
                errors = currentState.errors.copy(name = null)
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                errors = currentState.errors.copy(email = null)
            )
        }
    }

    fun onClaveChange(clave: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = clave,
                errors = currentState.errors.copy(password = null)
            )
        }
    }

    fun registrar() {
        val estadoActual = _uiState.value
        var nuevosErrores = UserErrors()
        var hayErrores = false

        if (estadoActual.name.isBlank()) {
            nuevosErrores = nuevosErrores.copy(name = "El nombre es obligatorio")
            hayErrores = true
        }
        if (estadoActual.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(estadoActual.email).matches()) {
            nuevosErrores = nuevosErrores.copy(email = "Introduce un email válido")
            hayErrores = true
        }
        if (estadoActual.password.length < 6) {
            nuevosErrores = nuevosErrores.copy(password = "La clave debe tener al menos 6 caracteres")
            hayErrores = true
        }

        if (hayErrores) {
            _uiState.update { it.copy(errors = nuevosErrores, succesfullSignUp = false) }
        } else {
            _uiState.update { it.copy(errors = UserErrors(), succesfullSignUp = true) }
        }
    }
}