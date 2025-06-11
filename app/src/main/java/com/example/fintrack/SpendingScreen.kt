package com.example.fintrack

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun SpendingScreen(navController: NavController) {
    val backStackEntry = remember {
        navController.getBackStackEntry("spending")
    }
    val categoryState = backStackEntry.savedStateHandle
        .getStateFlow("selected_category", "Clothing")
        .collectAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedType by remember { mutableStateOf("Income") }
    val transactionTypes = listOf("Income", "Spending")
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)) }
    var selectedTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)) }
    var amount by remember { mutableStateOf("") }
    val selectedCategory = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("selected_category", "Clothing")
        ?.collectAsState()
        ?.value ?: "Clothing"
    var notes by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Spending", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            transactionTypes.forEach {
                val isSelected = it == selectedType
                Button(
                    onClick = {
                        selectedType = it
                        if (it == "Income") {
                            navController.navigate("income") // navigasi ke SpendingScreen
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            when (it) {
                                "Income" -> Color.White
                                "Spending" -> Color(0xFFFFCCCC)
                                else -> Color.LightGray
                            }
                        } else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(it, color = if (isSelected) Color.Black else Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date") }
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
                            { _, hour, minute ->
                                selectedTime = "%02d:%02d".format(hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                label = { Text("Select Time") },
                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Time") }
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
            Icon(getCategoryIcon(selectedCategory), contentDescription = "Category", tint = Color.Red)
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
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        FloatingActionButton(
            onClick = { /* Save spending action */ },
            containerColor = Color(0xFFFFCCCC),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Submit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpendingScreenPreview() {
    val navController = rememberNavController() // Fake NavController
    SpendingScreen(navController = navController)
}
