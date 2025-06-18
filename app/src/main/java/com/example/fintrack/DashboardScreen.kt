package com.example.fintrack

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import components.MonthYearPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalSpending = MutableStateFlow(0.0)
    val totalSpending: StateFlow<Double> = _totalSpending

    private val _selectedMonthYear = MutableStateFlow(Pair(6, 2025))
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

        // Stop previous listeners
        incomeListener?.remove()
        spendingListener?.remove()

        incomeListener = db.collection("income")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val total = snapshot.sumOf {
                    val ts = it.getTimestamp("timestamp")?.toDate()
                    val amount = it.getDouble("amount") ?: 0.0
                    if (ts != null && isInMonth(ts, month, year)) amount else 0.0
                }
                _totalIncome.value = total
            }

        spendingListener = db.collection("spending")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val total = snapshot.sumOf {
                    val ts = it.getTimestamp("timestamp")?.toDate()
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Home", style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bulan dan navigasi
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

        // Kartu ringkasan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7EA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Income", fontWeight = FontWeight.Bold)
                    Text("IDR %.2f".format(totalIncome), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spending", fontWeight = FontWeight.Bold)
                    Text("IDR %.2f".format(totalSpending), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Balance
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDEEAFE)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Balance", fontWeight = FontWeight.Bold)
                Text("IDR %.2f".format(balance), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
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