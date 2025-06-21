package Screen

import android.annotation.SuppressLint
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SpendingViewModel : ViewModel() {
    private val _spendings = mutableStateListOf<Map<String, Any>>()  // Atau bikin data class
    val spendings: List<Map<String, Any>> get() = _spendings

    init {
        fetchSpendings()
    }

    fun fetchSpendings() {
        FirebaseFirestore.getInstance()
            .collection("spending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SpendingViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                _spendings.clear()
                for (doc in snapshots!!) {
                    _spendings.add(doc.data)
                }
            }
    }
}

class CategoryViewModel : ViewModel() {
    private val _categories = mutableStateListOf<String>()
    val categories: List<String> get() = _categories

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        val db = FirebaseFirestore.getInstance()
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                _categories.clear()
                for (document in result) {
                    val name = document.getString("name")
                    if (name != null) {
                        _categories.add(name)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("CategoryViewModel", "Error getting documents.", exception)
            }
    }

    fun addCategory(category: String) {
        val db = FirebaseFirestore.getInstance()
        val categoryData = hashMapOf("name" to category)

        db.collection("categories")
            .add(categoryData)
            .addOnSuccessListener {
                _categories.add(category)
            }
            .addOnFailureListener {
                Log.w("CategoryViewModel", "Failed to add category", it)
            }
    }
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun SpendingScreen(navController: NavController, viewModel: CategoryViewModel = viewModel()) {
    val categories = viewModel.categories

    val backStackEntry = remember {
        navController.getBackStackEntry("spending")
    }

    val selectedCategory = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("selected_category", "Clothing")
        ?.collectAsState()
        ?.value ?: "Clothing"

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedType by remember { mutableStateOf("Spending") }
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
            text = "Add Spending",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Income/Spending
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
                        if (it == "Income") navController.navigate("income")
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

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category
        Text("Category", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { navController.navigate("select_category") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(getCategoryIcon(selectedCategory), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(selectedCategory)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            placeholder = { Text("e.g. Grocery, Bill, etc.") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                val db = FirebaseFirestore.getInstance()
                val spendingData = hashMapOf(
                    "date" to selectedDate,
                    "time" to selectedTime,
                    "amount" to (amount.toDoubleOrNull() ?: 0.0),
                    "category" to selectedCategory,
                    "notes" to notes,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("spending")
                    .add(spendingData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Spending saved", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save", color = Color.White)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SpendingScreenPreview() {
    val navController = rememberNavController() // Fake NavController
    SpendingScreen(navController = navController)
}
