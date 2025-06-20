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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.text.NumberFormat
import java.util.Locale

class CategoriesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _categoryTotals = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryTotals: StateFlow<Map<String, Double>> = _categoryTotals

    private val _incomeTotal = MutableStateFlow(0.0)
    val incomeTotal: StateFlow<Double> = _incomeTotal

    init {
        // Listen spending
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

        // Listen income
        db.collection("income")
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                val totalIncome = snap.documents.sumOf {
                    it.getDouble("amount") ?: 0.0
                }
                _incomeTotal.value = totalIncome
            }
    }
}


@Composable
fun CategoryPieChart(viewModel: CategoriesViewModel = viewModel()) {
    val categoryTotalsState = viewModel.categoryTotals.collectAsState()
    val categoryTotals = categoryTotalsState.value
    val incomeTotalState = viewModel.incomeTotal.collectAsState()
    val incomeTotal = incomeTotalState.value
    val floatData: Map<String, Float> = categoryTotals.mapValues { it.value.toFloat() }

    if (floatData.isEmpty()) {
        Text("No data available")
    } else {
        SimplePieChart(data = floatData, incomeTotal = incomeTotal)
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
fun SimplePieChart(data: Map<String, Float>, incomeTotal: Double) {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFFF8A65)
    )

    val total = data.values.sum()
    if (total == 0f) {
        Text("No data to display", modifier = Modifier.padding(16.dp))
        return
    }

    val balance = incomeTotal - total
    val formattedBalance = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        .format(balance).replace("Rp", "Rp ")

    val maxCategory = data.maxByOrNull { it.value }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()), // ✅ scroll seluruh konten
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // ✅ Donut Chart & Balance
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Balance", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        formattedBalance,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                data.entries.forEachIndexed { index, entry ->
                    val percent = entry.value / total
                    val formattedAmount = currencyFormat.format(entry.value).replace("Rp", "Rp ")

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // ⬅️ Kategori dan Warna
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

                            // ➡️ Jumlah dan Persen
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formattedAmount,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "${(percent * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Bar indikator
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


            val safeIncome = if (incomeTotal <= 0.0) 1.0 else incomeTotal
            val balance = incomeTotal - total
            val hasNegativeBalance = balance < 0
            val mostSpent = data.maxByOrNull { it.value }
            val riskyCategories = data.filter { it.value > 0.3f * safeIncome }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "\uD83D\uDD0D Insight",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFD81B60)
                    )

                    if (hasNegativeBalance) {
                        Text(
                            text = "🔴 Your spending exceeds your income.",
                            fontSize = 13.sp,
                            color = Color(0xFFB71C1C)
                        )
                    }

                    mostSpent?.let {
                        val percent = (it.value / safeIncome) * 100
                        Text(
                            text = "🥇 Most spending goes to ${it.key} (${percent.toInt()}% of your income).",
                            fontSize = 13.sp,
                            color = Color(0xFF4A148C)
                        )
                    }

                    riskyCategories.forEach {
                        val percent = (it.value / safeIncome) * 100
                        Text(
                            text = "⚠️ Spending on ${it.key} is high: ${percent.toInt()}% of your income.",
                            fontSize = 13.sp,
                            color = Color(0xFF6A1B9A)
                        )
                    }

                    if (!hasNegativeBalance && riskyCategories.isEmpty()) {
                        Text(
                            text = "✅ Your spending is within safe limits. Good job!",
                            fontSize = 13.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}











