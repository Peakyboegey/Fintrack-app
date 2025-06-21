package Screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun saveIncomeToFirestore(date: String, time: String, amount: String, notes: String) {
    val db = FirebaseFirestore.getInstance()

    val incomeData = hashMapOf(
        "date" to date,
        "time" to time,
        "amount" to (amount.toDoubleOrNull() ?: 0.0),
        "notes" to notes,
        "timestamp" to Date()
    )

    db.collection("income")
        .add(incomeData)
        .addOnSuccessListener {
            Log.d("Firestore", "Income added with ID: ${it.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding income", e)
        }
}

@Composable
fun IncomeScreen(navController: NavController) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedType by remember { mutableStateOf("Income") }
    val transactionTypes = listOf("Income", "Spending")
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)) }
    var selectedTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Add Income",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Modern Toggle Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F0F0)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            transactionTypes.forEach {
                val isSelected = it == selectedType
                TextButton(
                    onClick = {
                        selectedType = it
                        if (it == "Spending") navController.navigate("spending")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = it,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Date & Time Pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
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
                    }
            )

            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time") },
                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
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
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            placeholder = { Text("e.g. Salary, Bonus, etc.") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                saveIncomeToFirestore(selectedDate, selectedTime, amount, notes)
                Toast.makeText(context, "Income saved", Toast.LENGTH_SHORT).show()
                navController.navigate("dashboard")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save", color = Color.White)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun IncomeScreenPreview() {
    val navController = rememberNavController() // Fake NavController
    IncomeScreen(navController = navController)
}