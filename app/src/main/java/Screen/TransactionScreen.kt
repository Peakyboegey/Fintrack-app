package Screen


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun TransactionScreen(navController: NavController) {
    // Ambil category dari savedStateHandle
    val backStackEntry = remember {
        navController.getBackStackEntry("transaction")
    }
    val categoryState = backStackEntry.savedStateHandle
        .getStateFlow("selected_category", "Clothing")
        .collectAsState()
    val transactionTypes = listOf("Income", "Spending")
    var selectedType by remember { mutableStateOf("Spending") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)) }
    var selectedTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)) }
    var amount by remember { mutableStateOf("6500.00") }
    val selectedCategory = categoryState.value
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Close, contentDescription = "Close")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Transaction", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            transactionTypes.forEach {
                val isSelected = it == selectedType
                Button(
                    onClick = {
                        selectedType = it

                        when (it) {
                            "Income" -> navController.navigate("income")
                            "Spending" -> navController.navigate("spending")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            when (it) {
                                "Income" -> Color(0xFFB8E5B8)
                                "Spending" -> Color(0xFFFFCCCC)
                                else -> Color.LightGray
                            }
                        } else Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(it, color = if (isSelected) Color.Black else Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = "%02d/%02d/%d".format(day, month + 1, year)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                label = { Text("Select Date") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _: TimePicker, hour: Int, minute: Int ->
                                selectedTime = "%02d:%02d".format(hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                label = { Text("Select Time") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = "Time")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            leadingIcon = { Text("$", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Category", color = Color(0xFF1A73E8), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("select_category") }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Checkroom, contentDescription = "Category", tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text(selectedCategory)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            placeholder = { Text("Optional details") },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = "Notes")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        FloatingActionButton(
            onClick = { /* Save action */ },
            containerColor = Color(0xFFE0E0FF),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Submit")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewTransactionScreen() {
    val navController = rememberNavController()
    TransactionScreen(navController = navController)
}
