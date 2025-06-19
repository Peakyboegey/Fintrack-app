package Navigation
import Screen.CategoryScreen
import Screen.DashboardScreen
import Screen.IncomeScreen
import Screen.SpendingScreen
import Screen.TransactionScreen
import Screen.WelcomeScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@RequiresApi(Build.VERSION_CODES.O)
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


