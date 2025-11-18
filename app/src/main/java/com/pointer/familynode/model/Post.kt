package com.pointer.familynode.model

// Representa un post obtenido desde la API
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
