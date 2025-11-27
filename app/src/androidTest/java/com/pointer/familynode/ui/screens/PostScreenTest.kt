package com.pointer.familynode.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.navigation.testing.TestNavHostController
import com.pointer.familynode.model.Post
import com.pointer.familynode.ui.MainActivity
import com.pointer.familynode.viewmodel.PostUiState
import org.junit.Rule
import org.junit.Test
import com.pointer.familynode.repository.PostRepository
import com.pointer.familynode.viewmodel.PostViewModel
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class PostScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun postsAreShown_whenUiStateHasPosts() {
        val posts = listOf(
            Post(userId = 1, id = 1, title = "primer post", body = "cuerpo1"),
            Post(userId = 1, id = 2, title = "segundo post", body = "cuerpo2")
        )

        val fakeViewModel = object : PostViewModel(mockk(relaxed = true)) {
            override val uiState: StateFlow<PostUiState> = MutableStateFlow(
                PostUiState(posts = posts, isLoading = false)
            ).asStateFlow()
        }

        val navController = TestNavHostController(composeRule.activity)

        composeRule.setContent {
            PostScreen(navController = navController, viewModel = fakeViewModel)
        }

        // Titles are capitalized in the UI
        composeRule.onNodeWithText("Primer post").assertIsDisplayed()
        composeRule.onNodeWithText("Segundo post").assertIsDisplayed()
    }

    @Test
    fun errorIsShown_whenUiStateHasError() {
        val fakeViewModel = object : PostViewModel(mockk(relaxed = true)) {
            override val uiState: StateFlow<PostUiState> = MutableStateFlow(
                PostUiState(error = "Fallo al cargar", isLoading = false)
            ).asStateFlow()
        }

        val navController = TestNavHostController(composeRule.activity)

        composeRule.setContent {
            PostScreen(navController = navController, viewModel = fakeViewModel)
        }

        composeRule.onNodeWithText("¡Vaya, pajaron! Algo salió mal:").assertIsDisplayed()
        composeRule.onNodeWithText("Fallo al cargar").assertIsDisplayed()
    }
}
