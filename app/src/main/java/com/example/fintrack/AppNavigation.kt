package com.example.fintrack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable("welcome") { WelcomeScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("transaction") { TransactionScreen(navController) }
        composable("income") { IncomeScreen(navController) }
        composable("spending") { SpendingScreen(navController) }
        composable("select_category") {
            CategoryScreen(navController = navController) { selectedCategory ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_category", selectedCategory)
                navController.popBackStack()
            }
        }
    }
}


