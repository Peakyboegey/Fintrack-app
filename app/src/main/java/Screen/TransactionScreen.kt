package Screen


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.TimePicker
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class Transaction(
    val amount: Double=0.2,
    val date: String="",
    val time: String="",
    val notes: String="",
    val category: String?= null,
    val timestamp: com.google.firebase.Timestamp? = null,
    val type: String=""
)

class TransactionViewModel : ViewModel(){
    private val db= FirebaseFirestore.getInstance()

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    init {
        fetchAllTransactions()
    }

    private fun fetchAllTransactions(){
        val spendingRef = db.collection("spending").orderBy("timestamp", Query.Direction.DESCENDING)
        val incomeRef = db.collection("income").orderBy("timestamp", Query.Direction.DESCENDING)

        val spendingTask = spendingRef.get()
        val incomeTask = incomeRef.get()

        Tasks.whenAllSuccess<QuerySnapshot>(spendingTask, incomeTask)
            .addOnSuccessListener { results->
                val allTransactions = mutableListOf<Transaction>()
            }
    }
}

fun groupTransactionsByDate(transactions: List<Transaction>): Map<LocalDate, List<Transaction>> {
    return transactions.groupBy { it.date }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun TransactionScreen(transactions: List<Transaction>) {
    val grouped = groupTransactionsByDate(transactions).toSortedMap(reverseOrder())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        grouped.forEach { (date, dailyTransactions) ->
            item {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(dailyTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = transaction.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (transaction.type == TransactionType.INCOME) Color.Green else Color.Red
                )
            }
            Text(
                text = "Rp ${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    val sampleTransactions = listOf(
        Transaction(1, "Gaji Bulanan", 5000000.0, TransactionType.INCOME, LocalDate.of(2025, 6, 18)),
        Transaction(2, "Beli Kopi", 25000.0, TransactionType.SPENDING, LocalDate.of(2025, 6, 18)),
        Transaction(3, "Beli Buku", 150000.0, TransactionType.SPENDING, LocalDate.of(2025, 6, 17)),
        Transaction(4, "Freelance", 750000.0, TransactionType.INCOME, LocalDate.of(2025, 6, 17))
    )

    MaterialTheme {
        TransactionScreen(sampleTransactions)
    }
}
