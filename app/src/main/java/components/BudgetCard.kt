package components

import Screen.Budget
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetCard(budget: Budget) {
    val context = LocalContext.current
    val hasBudget = budget.budgetAmount > 0
    val progress = if (hasBudget) {
        (budget.usedAmount / budget.budgetAmount).coerceIn(0.0, 1.0)
    } else 0.0
    val overBudget = budget.usedAmount > budget.budgetAmount && hasBudget
    val nearlyExceeded = progress in 0.8..1.0 && !overBudget

    // ✅ Tampilkan notifikasi saat mendekati atau melebihi budget
    LaunchedEffect(key1 = budget.usedAmount) {
        if (overBudget) {
            Toast.makeText(context, "⚠️ ${budget.category} budget exceeded!", Toast.LENGTH_SHORT).show()
        } else if (nearlyExceeded) {
            Toast.makeText(context, "⚠️ ${budget.category} spending is nearing its limit.", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (overBudget) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = budget.category,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (overBudget) Color(0xFFD32F2F) else Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (progress < 1f) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Used: Rp${budget.usedAmount.toInt()} / Rp${budget.budgetAmount.toInt()}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (overBudget) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Warning: Budget exceeded!",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}


