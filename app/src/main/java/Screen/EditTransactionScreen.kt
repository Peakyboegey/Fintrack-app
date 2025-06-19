package Screen

import Screen.TransactionScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


@Composable
fun EditTransactionScreen(navController: NavHostController, type: String, id: String) {
    val viewModel: TransactionViewModel = viewModel()
    var transaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(id) {
        viewModel.getTransactionById(type, id) {
            transaction = it
        }
    }

    transaction?.let { trx ->
        var amount by remember { mutableStateOf(trx.amount.toString()) }
        var notes by remember { mutableStateOf(trx.notes) }

        Column(Modifier.padding(16.dp)) {
            Text("Edit Transaction", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.updateTransaction(trx.copy(
                        amount = amount.toDoubleOrNull() ?: trx.amount,
                        notes = notes
                    ))
                    navController.popBackStack()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save")
            }
        }
    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
