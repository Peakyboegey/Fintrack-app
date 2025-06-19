package components

import Screen.CategoriesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Pie Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
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
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    floatData.entries.forEachIndexed { index, (category, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[index % colors.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$category (${value.toInt()})",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}



