package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun BottomNavBar(
    navController: NavController,
    onFabClick: () -> Unit
) {
    val items = listOf(
        "Home" to Icons.Default.Home,
        "Analysis" to Icons.Default.PieChart,
        "Transaction" to Icons.Default.SwapHoriz,
        "Categories" to Icons.Default.Category
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = Color(0xFFDCD9FF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFEFEFFF)) {
                items.forEach { (label, icon) ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(label) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
        }
    }
}

@Composable
fun FabMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = (-40).dp, y = (-60).dp)
    ) {
        DropdownMenuItem(
            text = { Text("Add Transaction") },
            onClick = {
                onDismiss()
                onNavigate("AddTransaction")
            }
        )
        DropdownMenuItem(
            text = { Text("Add Category") },
            onClick = {
                onDismiss()
                onNavigate("AddCategory")
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun FintrackDashboardPreview() {
    val navController = rememberNavController()
    var fabExpanded by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    onFabClick = { fabExpanded = true }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Konten utama dashboard
            }

            FabMenu(
                expanded = fabExpanded,
                onDismiss = { fabExpanded = false },
                onNavigate = { /* implementasi navigasi */ }
            )
        }
    }
}
