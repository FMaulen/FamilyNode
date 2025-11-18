package com.pointer.familynode.repository

import com.pointer.familynode.data.remote.RetrofitInstance
import com.pointer.familynode.model.Post

// Este repositorio se encarga de acceder a los datos usando Retrofit
class PostRepository {
    // Funcion que obtiene los posts desde la API
    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }
}
