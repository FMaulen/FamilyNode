package com.pointer.familynode.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import com.pointer.familynode.model.Post
import com.pointer.familynode.repository.PostRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
class PostViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState updates correctly on success`() = runTest {
        val mockRepo = mockk<PostRepository>()
        coEvery { mockRepo.getPosts() } returns listOf(Post(1, 1, "uno", "cuerpo"))
        val vm = PostViewModel(repository = mockRepo)

        // advance until launched coroutine finishes
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.posts.size)
        assertEquals("uno", state.posts[0].title)
    }

    @Test
    fun `uiState updates with error on failure`() = runTest {
        val mockRepo = mockk<PostRepository>()
        coEvery { mockRepo.getPosts() } throws RuntimeException("boom")

        val vm = PostViewModel(repository = mockRepo)

        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.posts.isEmpty())
    }
}
