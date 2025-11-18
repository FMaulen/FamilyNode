package com.pointer.familynode.data.remote

import com.pointer.familynode.model.Post
import retrofit2.http.GET

// define los endpoints HTTP
interface ApiService {
    // Define una solicitud GET al endpoint /posts
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
