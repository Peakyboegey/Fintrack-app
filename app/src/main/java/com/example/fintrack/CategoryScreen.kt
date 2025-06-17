package com.example.fintrack

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.FirebaseFirestore


fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "clothing" -> Icons.Default.Checkroom
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "utilities" -> Icons.Default.Bolt
        else -> Icons.Default.Category // default icon
    }
}

@Composable
fun CategoryScreen(navController: NavController, onCategorySelected: (String) -> Unit) {
    var categories by remember { mutableStateOf(listOf("Clothing", "Food", "Transport", "Utilities")) }
    var newCategory by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        // Row untuk tombol Quit
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                navController.navigate("spending") {
                    popUpTo("category") { inclusive = true } // hapus halaman kategori dari stack jika mau
                }
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text("Select a Category", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_category", category)
                        navController.popBackStack()
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(getCategoryIcon(category), contentDescription = category)
                Spacer(modifier = Modifier.width(8.dp))
                Text(category, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Add New Category", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newCategory,
            onValueChange = { newCategory = it },
            label = { Text("New Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val trimmed = newCategory.trim()
                if (trimmed.isNotEmpty() && !categories.contains(trimmed)) {
                    categories = categories + trimmed
                    addCategoryToFirestore(trimmed) // simpan ke Firestore
                    newCategory = ""
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = newCategory.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
        }
    }
}

fun addCategoryToFirestore(category: String) {
    val db = FirebaseFirestore.getInstance()
    val categoryData = hashMapOf("name" to category)

    db.collection("categories")
        .add(categoryData)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "Category added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding category", e)
        }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    val navController = rememberNavController()

    CategoryScreen(
        navController = navController,
        onCategorySelected = { /* Preview: no-op */ }
    )
}
