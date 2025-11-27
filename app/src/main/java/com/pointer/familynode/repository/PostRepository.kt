package com.pointer.familynode.repository

import com.pointer.familynode.data.remote.ApiService
import com.pointer.familynode.data.remote.RetrofitInstance
import com.pointer.familynode.model.Post


class PostRepository(private val apiService: ApiService = RetrofitInstance.api) {
    suspend fun getPosts(): List<Post> {
        return apiService.getPosts()
    }
}