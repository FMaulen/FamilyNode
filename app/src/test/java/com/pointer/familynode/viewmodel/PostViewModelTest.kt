package com.pointer.familynode.viewmodel

import com.pointer.familynode.model.Post
import com.pointer.familynode.repository.PostRepository
import io.mockk.coEvery
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PostViewModelTest {

    @Before
    fun setup() {
        // nothing here; set Main in each test
    }

    @After
    fun tearDown() {
        try {
            unmockkConstructor(PostRepository::class)
        } catch (_: Exception) {
        }
        resetMain()
    }

    @Test
    fun `uiState updates correctly on success`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        mockkConstructor(PostRepository::class)
        coEvery { anyConstructed<PostRepository>().getPosts() } returns listOf(Post(1, 1, "uno", "cuerpo"))

        val vm = PostViewModel()

        // advance until launched coroutine finishes
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.posts.size)
        assertEquals("uno", state.posts[0].title)
    }

    @Test
    fun `uiState updates with error on failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        mockkConstructor(PostRepository::class)
        coEvery { anyConstructed<PostRepository>().getPosts() } throws RuntimeException("boom")

        val vm = PostViewModel()

        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.posts.isEmpty())
    }
}
