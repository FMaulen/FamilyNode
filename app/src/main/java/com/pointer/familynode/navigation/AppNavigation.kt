package com.pointer.familynode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.pointer.familynode.ui.screens.EditNoteScreen
import com.pointer.familynode.ui.screens.HomeScreen
import com.pointer.familynode.ui.screens.LoginScreen
import com.pointer.familynode.ui.screens.SignUpScreen
import com.pointer.familynode.viewmodel.HomeViewModel
import com.pointer.familynode.viewmodel.LoginViewModel
import com.pointer.familynode.viewmodel.SignUpViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("signup") {
            val signUpViewModel: SignUpViewModel = viewModel()
            SignUpScreen(navController = navController, viewModel = signUpViewModel)
        }
        composable("posts") {
            val postViewModel: PostViewModel = viewModel()
            PostScreen(navController = navController, viewModel = postViewModel)
        }

        navigation(startDestination = "home", route = "main_flow") {

            composable("home") { navBackStackEntry ->
                val parentEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val homeViewModel: HomeViewModel = viewModel(parentEntry)
                HomeScreen(navController = navController, viewModel = homeViewModel)
            }

            composable("note/{noteId}") { navBackStackEntry ->
                val noteId = navBackStackEntry.arguments?.getString("noteId")
                val parentEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val homeViewModel: HomeViewModel = viewModel(parentEntry)
                EditNoteScreen(
                    navController = navController,
                    viewModel = homeViewModel,
                    noteId = noteId
                )
            }
        }
    }
}