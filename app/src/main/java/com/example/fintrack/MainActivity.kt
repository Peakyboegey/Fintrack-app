package com.example.fintrack

import Screen.BudgetScreen
import Screen.CategoriesScreen
import Screen.CategoryScreen
import Screen.DashboardScreen
import Screen.EditTransactionScreen
import Screen.IncomeScreen
import Screen.SpendingScreen
import Screen.TransactionScreen
import Screen.WelcomeScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.fintrack.ui.theme.FintrackTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import components.BottomNavBar
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)

        setContent {

            FintrackTheme {
                val navController = rememberNavController()

                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    "dashboard", "home", "analysis", "transaction", "categories", "income", "spending", "budgeting"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (showBottomBar) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate("income")
                                },
                                containerColor = Color(0xFFDCD9FF)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                navController = navController,
                                onFabClick = {
                                    navController.navigate("transactions")
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("welcome") {
                            WelcomeScreen(navController)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController)
                        }
                        composable("income") {
                            IncomeScreen(navController)
                        }
                        composable("spending") {
                            SpendingScreen(navController)
                        }
                        composable("select_category") {
                            CategoryScreen(navController = navController) { selectedCategory ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selected_category", selectedCategory)
                                navController.popBackStack()
                            }
                        }
                        composable("categories") {
                            CategoriesScreen(navController)
                        }
                        composable("transaction"){
                            TransactionScreen(navController)
                        }
                        composable(
                            "EditTransactionScreen/{type}/{id}",
                            arguments = listOf(
                                navArgument("type") { type = NavType.StringType },
                                navArgument("id") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type") ?: ""
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            EditTransactionScreen(navController, type, id)
                        }

                        composable("budgeting") {
                            BudgetScreen() //
                        }

                    }
                }
            }
        }
    }
}



