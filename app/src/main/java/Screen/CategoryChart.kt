package Screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CategoriesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _categoryTotals = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryTotals: StateFlow<Map<String, Double>> = _categoryTotals

    init {
        db.collection("spending")
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                val totals = snap.documents
                    .mapNotNull { doc ->
                        val cat = doc.getString("category") ?: "Unknown"
                        val amt = doc.getDouble("amount") ?: 0.0
                        cat to amt
                    }
                    .groupingBy { it.first }
                    .fold(0.0) { acc, e -> acc + e.second }
                _categoryTotals.value = totals
            }
    }
}

@Composable
fun CategoryPieChart(viewModel: CategoriesViewModel = viewModel()) {
    val categoryTotalsState = viewModel.categoryTotals.collectAsState()
    val categoryTotals = categoryTotalsState.value

    val floatData = categoryTotals.mapValues { it.value.toFloat() }

    if (floatData.isEmpty()) {
        Text("No data available")
    } else {
        SimplePieChart(floatData)
    }
}

@Composable
fun CategoriesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Categories", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        CategoryPieChart()
    }
}

@Composable
fun SimplePieChart(data: Map<String, Float>) {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFFF8A65)
    )

    val total = data.values.sum()
    if (total == 0f) {
        Text("No data to display", modifier = Modifier.padding(16.dp))
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    var startAngle = -90f
                    data.entries.forEachIndexed { index, entry ->
                        val sweep = 360f * (entry.value / total)
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 24f, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
            }

            // Legend + Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.entries.forEachIndexed { index, entry ->
                    val percent = entry.value / total
                    Column {
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
                                        .background(colors[index % colors.size])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.key,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${(percent * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        LinearProgressIndicator(
                            progress = percent,
                            color = colors[index % colors.size],
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }
    }
}











