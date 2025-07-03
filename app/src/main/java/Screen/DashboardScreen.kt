package Screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import components.MiniCategoryChart
import components.MonthYearPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.verticalScroll
import kotlin.Pair
import components.BudgetCard


class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalSpending = MutableStateFlow(0.0)
    val totalSpending: StateFlow<Double> = _totalSpending

    private fun currentMonthYear(): Pair<Int, Int> {
        val now = Calendar.getInstance()
        return Pair(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR))
    }

    private val _selectedMonthYear = MutableStateFlow(currentMonthYear())
    val selectedMonthYear: StateFlow<Pair<Int, Int>> = _selectedMonthYear



    val balance: StateFlow<Double> = combine(_totalIncome, _totalSpending) { income, spending ->
        income - spending
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private var incomeListener: ListenerRegistration? = null
    private var spendingListener: ListenerRegistration? = null

    init {
        fetchDataForMonth()
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonthYear.value = Pair(month, year)
        fetchDataForMonth()
    }

    private fun fetchDataForMonth() {
        val (month, year) = _selectedMonthYear.value

        incomeListener?.remove()
        spendingListener?.remove()

        incomeListener = db.collection("income")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val total = snapshot.sumOf {
                    val ts = it.getDate("timestamp") // ✅ lebih stabil
                    val amount = it.getDouble("amount") ?: 0.0
                    if (ts != null && isInMonth(ts, month, year)) amount else 0.0
                }
                _totalIncome.value = total
            }

        spendingListener = db.collection("spending")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val total = snapshot.sumOf {
                    val ts = it.getDate("timestamp") // ✅ ganti ini juga
                    val amount = it.getDouble("amount") ?: 0.0
                    if (ts != null && isInMonth(ts, month, year)) amount else 0.0
                }
                _totalSpending.value = total
            }
    }


    private fun isInMonth(date: Date, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year
    }

    override fun onCleared() {
        incomeListener?.remove()
        spendingListener?.remove()
        super.onCleared()
    }
}


@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsState()
    val rupiahFormat = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    val transactionViewModel: TransactionViewModel = viewModel()
    val transactions = transactionViewModel.transactions.take(5)
    val budgetViewModel: BudgetViewModel = viewModel()
    val budgets = budgetViewModel.budgets


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFFDFDFD))
            .padding(16.dp)
    ) {
        // ✅ Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Bulan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonthYearPicker(
                selectedDate = "${monthName(selectedMonthYear.first)} ${selectedMonthYear.second}",
                onDateSelected = { newDate ->
                    val parts = newDate.split(" ")
                    val month = monthNameToNumber(parts[0])
                    val year = parts[1].toIntOrNull() ?: 2025
                    viewModel.setMonthYear(month, year)
                }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.Gray)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Ringkasan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "💰 Income",
                amount = rupiahFormat.format(totalIncome).replace("Rp", "Rp "),
                backgroundColor = Color(0xFFD0F0C0),
                icon = Icons.Default.ArrowDropUp,
                iconColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "🛍️ Spending",
                amount = rupiahFormat.format(totalSpending).replace("Rp", "Rp "),
                backgroundColor = Color(0xFFFFE0E0),
                icon = Icons.Default.ArrowDropDown,
                iconColor = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )

        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Your Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(12.dp))

        budgets.forEach { budget ->
            BudgetCard(budget = budget)
        }

        // ✅ Balance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDEEAFE)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🧾 Balance", fontWeight = FontWeight.Bold)
                Text(
                    rupiahFormat.format(balance).replace("Rp", "Rp "),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Mini Pie Chart / Category Breakdown
        MiniCategoryChart()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = transaction.category ?: "No Category",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (transaction.type == "income") "Income" else "Spending",
                            fontSize = 12.sp,
                            color = if (transaction.type == "income") Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                    Text(
                        text = "Rp ${transaction.amount.toInt()}",
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == "income") Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
            }

            if (transactions.isEmpty()) {
                Text("No transactions yet.", color = Color.Gray, fontSize = 13.sp)
            }
        }

    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    backgroundColor: Color,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier // Tambahkan ini agar bisa dikontrol dari luar
) {
    Card(
        modifier = modifier, // ✅ Gunakan modifier dari parameter
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(amount, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}



fun monthName(month: Int): String {
    return listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )[month - 1]
}

fun monthNameToNumber(name: String): Int {
    val map = mapOf(
        "January" to 1, "February" to 2, "March" to 3, "April" to 4,
        "May" to 5, "June" to 6, "July" to 7, "August" to 8,
        "September" to 9, "October" to 10, "November" to 11, "December" to 12
    )
    return map[name] ?: 1
}





@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun FintrackDashboardPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        DashboardScreen(navController = navController)
    }
}