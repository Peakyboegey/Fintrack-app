package Screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon // ✅ Bukan yang dari material (lama)
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Date


class BudgetViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var budgets by mutableStateOf<List<Budget>>(emptyList())
        private set

    private val _selectedMonthYear = MutableStateFlow(currentMonthYear())
    val selectedMonthYear: StateFlow<Pair<Int, Int>> = _selectedMonthYear

    init {
        fetchBudgets()
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonthYear.value = Pair(month, year)
        fetchBudgets()
    }

    private fun fetchBudgets() {
        val (month, year) = _selectedMonthYear.value
        db.collection("budgeting")
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { snapshot ->
                budgets = snapshot.documents.mapNotNull { doc ->
                    val budget = doc.toObject(Budget::class.java)
                    budget?.copy(category = doc.getString("category") ?: "", id = doc.id)
                }
                updateUsedAmounts()
            }
    }

    fun refreshBudgets() {
        fetchBudgets()
    }

    private fun updateUsedAmounts() {
        val (month, year) = _selectedMonthYear.value
        db.collection("spending")
            .get()
            .addOnSuccessListener { snapshot ->
                val updated = budgets.map { budget ->
                    val totalSpending = snapshot.documents.sumOf { doc ->
                        val cat = doc.getString("category") ?: ""
                        val ts = doc.getDate("timestamp")
                        val amt = doc.getDouble("amount") ?: 0.0
                        if (cat == budget.category && ts != null && isInMonth(ts, month, year)) amt else 0.0
                    }
                    budget.copy(usedAmount = totalSpending)
                }
                budgets = updated
            }
    }

    fun deleteBudget(budget: Budget) {
        db.collection("budgeting").document(budget.id)
            .delete()
            .addOnSuccessListener { fetchBudgets() }
    }

    private fun isInMonth(date: Date, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
    }

    private fun currentMonthYear(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return Pair(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }
}

data class Budget(
    val id: String = "",
    val category: String = "",
    val budgetAmount: Double = 0.0,
    val usedAmount: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val budgets = viewModel.budgets
    val categories = categoryViewModel.categories

    var selectedCategory by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "📊 Budgeting",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // === Select Category ===
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // === Budget Input ===
        OutlinedTextField(
            value = budgetInput,
            onValueChange = { budgetInput = it },
            label = { Text("Budget Amount (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val budgetAmount = budgetInput.toDoubleOrNull() ?: 0.0
                if (selectedCategory.isNotEmpty() && budgetAmount > 0.0) {
                    val (month, year) = viewModel.selectedMonthYear.value
                    val newBudget = Budget(
                        category = selectedCategory,
                        budgetAmount = budgetAmount,
                        usedAmount = 0.0,
                        month = month,
                        year = year
                    )
                    FirebaseFirestore.getInstance().collection("budgeting")
                        .add(newBudget)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Budget saved", Toast.LENGTH_SHORT).show()
                            viewModel.refreshBudgets()
                            budgetInput = ""
                            selectedCategory = ""
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Please fill correctly", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Save Budget")
        }

        Spacer(modifier = Modifier.height(26.dp))

        // === Budget Cards ===
        budgets.forEach { budget ->
            val progress = if (budget.budgetAmount > 0) {
                (budget.usedAmount / budget.budgetAmount).coerceIn(0.0, 1.0)
            } else 0.0
            val over = budget.usedAmount > budget.budgetAmount

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (over) Color(0xFFFFF1F1) else Color(0xFFF7F7F7)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = budget.category,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )

                        IconButton(onClick = {
                            viewModel.deleteBudget(budget)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (over) Color(0xFFD32F2F) else Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "Used: Rp${budget.usedAmount.toInt()} / Rp${budget.budgetAmount.toInt()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (over) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Budget exceeded!",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }

        if (budgets.isEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))
            Text("No budget data available.", color = Color.Gray)
        }
    }
}



