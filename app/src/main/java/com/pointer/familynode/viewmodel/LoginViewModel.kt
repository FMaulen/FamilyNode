package com.pointer.familynode.viewmodel

import androidx.lifecycle.ViewModel
import com.pointer.familynode.model.UserErrors
import com.pointer.familynode.model.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState (
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val loginSuccessful: Boolean = false
)

class LoginViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.update { it.copy(error = "El usuario y la contraseña no pueden estar vacios.") }
            return
        }

        _uiState.update { it.copy(loginSuccessful = true, error = null) }
    }
}