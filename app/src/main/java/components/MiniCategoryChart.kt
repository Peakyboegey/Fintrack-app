package components

import Screen.CategoriesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun MiniCategoryChart(viewModel: CategoriesViewModel = viewModel()) {
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val floatData = categoryTotals.mapValues { it.value.toFloat() }

    if (floatData.isEmpty()) {
        Text("No spending data", style = MaterialTheme.typography.bodySmall)
    } else {
        val colors = listOf(
            Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
            Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFFF8A65)
        )

        val total = floatData.values.sum()
        var startAngle = 0f

        Column {
            Text(
                "Spending by Category",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier.size(80.dp)
                    ) {
                        floatData.entries.forEachIndexed { index, entry ->
                            val sweep = 360f * (entry.value / total)
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true
                            )
                            startAngle += sweep
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(floatData.entries.toList()) { (category, _) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(colors[category.hashCode() % colors.size])
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


