package com.pointer.familynode.repository

import com.pointer.familynode.data.remote.RetrofitInstance
import com.pointer.familynode.model.Post
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class PostRepositoryTest {

    @Test
    fun `getPosts returns list when api succeeds`() = runTest {
        val mockApi = mockk<com.pointer.familynode.data.remote.ApiService>()
        mockkObject(RetrofitInstance)
        coEvery { mockApi.getPosts() } returns listOf(Post(1, 1, "titulo uno", "cuerpo"))
        every { RetrofitInstance.api } returns mockApi

        val repo = PostRepository()
        val result = repo.getPosts()

        assertEquals(1, result.size)
        assertEquals("titulo uno", result[0].title)
    }

    @Test(expected = IOException::class)
    fun `getPosts throws when api fails`() = runTest {
        val mockApi = mockk<com.pointer.familynode.data.remote.ApiService>()
        mockkObject(RetrofitInstance)
        coEvery { mockApi.getPosts() } throws IOException("network")
        every { RetrofitInstance.api } returns mockApi

        val repo = PostRepository()
        repo.getPosts() // should throw
    }
}
