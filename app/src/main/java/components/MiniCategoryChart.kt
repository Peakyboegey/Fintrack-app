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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun MiniCategoryChart(viewModel: CategoriesViewModel = viewModel()) {
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val floatData = categoryTotals.mapValues { it.value.toFloat() }

    if (floatData.isEmpty()) {
        Text(
            "Tidak ada data pengeluaran",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val categoryColors = listOf(
        Color(0xFFEF5350), // Merah
        Color(0xFF42A5F5), // Biru
        Color(0xFF66BB6A), // Hijau
        Color(0xFFFFA726), // Oranye
        Color(0xFFAB47BC), // Ungu
        Color(0xFFFFD54F), // Kuning
        Color(0xFF26A69A), // Teal
        Color(0xFF8D6E63)  // Cokelat
    )

    val total = floatData.values.sum()
    var startAngle = -90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Spending on Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Donut chart
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
                            color = categoryColors[index % categoryColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 24f, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
            }

            Divider()

            // Legend (kategori dan nominal)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                floatData.entries.forEachIndexed { index, entry ->
                    val color = categoryColors[index % categoryColors.size]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.key,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Rp ${entry.value.toInt()}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}





