package com.pointer.familynode.repository

import com.pointer.familynode.data.remote.ApiService
import com.pointer.familynode.model.Post

// Este repositorio se encarga de acceder a los datos usando Retrofit
class PostRepository (private val apiService: ApiService) {
    // Funcion que obtiene los posts desde la API
    suspend fun getPosts(): List<Post> {
        return apiService.getPosts()
    }
}
