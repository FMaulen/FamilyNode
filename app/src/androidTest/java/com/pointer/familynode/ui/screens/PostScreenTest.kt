package com.pointer.familynode.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.navigation.testing.TestNavHostController
import com.pointer.familynode.model.Post
import com.pointer.familynode.ui.MainActivity
import com.pointer.familynode.viewmodel.PostUiState
import org.junit.Rule
import org.junit.Test

class PostScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun postsAreShown_whenUiStateHasPosts() {
        val posts = listOf(
            Post(userId = 1, id = 1, title = "primer post", body = "cuerpo1"),
            Post(userId = 1, id = 2, title = "segundo post", body = "cuerpo2")
        )

        val previewState = PostUiState(posts = posts, isLoading = false)

        val navController = TestNavHostController(composeRule.activity)

        composeRule.setContent {
            PostScreen(navController = navController, previewUiState = previewState)
        }

        // Titles are capitalized in the UI
        composeRule.onNodeWithText("Primer post").assertIsDisplayed()
        composeRule.onNodeWithText("Segundo post").assertIsDisplayed()
    }

    @Test
    fun errorIsShown_whenUiStateHasError() {
        val previewState = PostUiState(error = "Fallo al cargar", isLoading = false)
        val navController = TestNavHostController(composeRule.activity)

        composeRule.setContent {
            PostScreen(navController = navController, previewUiState = previewState)
        }

        composeRule.onNodeWithText("¡Vaya, pajaron! Algo salió mal:").assertIsDisplayed()
        composeRule.onNodeWithText("Fallo al cargar").assertIsDisplayed()
    }
}
