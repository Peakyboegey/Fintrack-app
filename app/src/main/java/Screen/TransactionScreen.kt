package Screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.DocumentId
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit


data class Transaction(
    val amount: Double=0.0,
    val date: String="",
    val time: String="",
    val notes: String="",
    val category: String?= null,
    val timestamp: com.google.firebase.Timestamp? = null,
    val type: String="",
    val documentId: String=""
)

class TransactionViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    init {
        fetchAllTransactions()
    }

    private fun fetchAllTransactions() {
        val spendingRef = db.collection("spending").orderBy("timestamp", Query.Direction.DESCENDING)
        val incomeRef = db.collection("income").orderBy("timestamp", Query.Direction.DESCENDING)

        val spendingTask = spendingRef.get()
        val incomeTask = incomeRef.get()

        Tasks.whenAllSuccess<QuerySnapshot>(spendingTask, incomeTask)
            .addOnSuccessListener { results ->
                val allTransactions = mutableListOf<Transaction>()

                val spendingDocs = results[0] as QuerySnapshot
                val incomeDocs = results[1] as QuerySnapshot

                spendingDocs.documents.mapNotNullTo(allTransactions) { doc ->
                    doc.toObject(Transaction::class.java)?.copy(
                        type = "spending",
                        documentId = doc.id
                    )
                }

                incomeDocs.documents.mapNotNullTo(allTransactions) { doc ->
                    doc.toObject(Transaction::class.java)?.copy(
                        type = "income",
                        documentId = doc.id
                    )
                }

                // Sort by timestamp descending
                transactions = allTransactions.sortedByDescending { it.timestamp }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch transactions", e)
            }

        }

    fun deleteTransaction(transaction: Transaction) {
        val collection = db.collection(transaction.type)
        collection.document(transaction.documentId)
            .delete()
            .addOnSuccessListener {
                transactions = transactions.filterNot { it.documentId == transaction.documentId }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to delete document.", it)
            }
    }

    fun getTransactionById(
        type: String,
        id: String,
        onResult: (Transaction?) -> Unit
    )   {
        db.collection(type).document(id).get()
            .addOnSuccessListener { doc ->
                val trx = doc.toObject(Transaction::class.java)?.copy(
                    type = type,
                    documentId = id
                )
                    onResult(trx)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to edit transaction.", it)
                onResult(null)
            }

    }

    fun updateTransaction(transaction: Transaction) {
        db.collection(transaction.type)
            .document(transaction.documentId)
            .update(
                mapOf(
                    "amount" to transaction.amount,
                    "notes" to transaction.notes
                )
            )
            .addOnSuccessListener {
                fetchAllTransactions()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Update Transaction Failed", it)
            }
    }

}



@RequiresApi(Build.VERSION_CODES.O)
fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
    return transactions.groupBy { it.date }
        .toSortedMap(compareByDescending { dateStr ->
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionScreen(navController: NavHostController) {
    val viewModel: TransactionViewModel = viewModel()
    val transactions = viewModel.transactions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction") },
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val grouped = groupTransactionsByDate(transactions)

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                grouped.forEach { (date, dailyTransactions) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(dailyTransactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDeleteClick = {viewModel.deleteTransaction(it)},
                            onEditClick = {
                                navController.navigate("EditTransactionScreen/${it.type}/${it.documentId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onEditClick: (Transaction) -> Unit = {},
    onDeleteClick: (Transaction) -> Unit ={}
    ){
    val typeColor = if (transaction.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
    val label = if (transaction.type == "income") "Income" else "Spending"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = label, color = typeColor, fontWeight = FontWeight.Bold)
                    transaction.category?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row{
                    IconButton(onClick = {onEditClick(transaction)}) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {onDeleteClick(transaction)}) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }

                Text(
                    text = "Rp ${transaction.amount}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = transaction.notes, style = MaterialTheme.typography.bodySmall)
                Text(text = transaction.time, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TransactionScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        TransactionScreen(navController = navController)
    }
}

