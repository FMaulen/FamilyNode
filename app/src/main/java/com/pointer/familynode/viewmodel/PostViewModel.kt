package com.pointer.familynode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointer.familynode.model.Post
import com.pointer.familynode.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

open class PostViewModel(private val repository: PostRepository = PostRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    open val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    init {
        fetchPosts()
    }


    private fun fetchPosts() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val posts = repository.getPosts()
                _uiState.update { it.copy(posts = posts, isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al obtener los datos: ${e.message}", isLoading = false) }
                // e.printStackTrace() // Es mejor no tener esto en el ViewModel
            }
        }
    }
}