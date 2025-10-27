package com.pointer.familynode.model

data class UserUiState (
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errors: UserErrors = UserErrors(),
    val succesfullSignUp: Boolean = false
)

data class UserErrors (
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
)
