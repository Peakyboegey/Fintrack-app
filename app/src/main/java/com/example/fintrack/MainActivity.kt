package com.example.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.fintrack.ui.theme.FintrackTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import components.BottomNavBar
import androidx.compose.runtime.getValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FintrackTheme {
                val navController = rememberNavController()

                // Dapatkan rute saat ini untuk kontrol tampilan bottom bar
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // Tentukan kapan BottomNavBar ditampilkan
                val showBottomBar = currentRoute in listOf(
                    "dashboard", "home", "analysis", "transaction", "categories", "dashboard", "home", "analysis", "transaction", "categories", "income", "spending"
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
                                    navController.navigate("transaction")
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
                        composable("transaction") {
                            TransactionScreen(navController)
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

                    }
                }
            }
        }
    }
}



