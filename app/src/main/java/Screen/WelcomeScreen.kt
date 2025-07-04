package Screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.fintrack.R


@Composable
fun WelcomeScreen(navController: NavController) {
    val backgroundColor = Color(0xFFE3FCE5) // Hijau lembut
    val primaryTextColor = Color(0xFF1B5E20) // Hijau tua

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo Placeholder (bisa diganti Image jika punya aset logo)
        Image(
            painter = painterResource(id = R.drawable.smartphone),
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp)
        )


        // Judul dan Deskripsi
        Column {
            Text(
                text = "Track Your\nSpending\nEffortlessly",
                color = primaryTextColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Manage your finances with ease using our clean, intuitive interface. Set goals, track progress, and take control.",
                color = primaryTextColor.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        // Tombol Get Started saja
        Button(
            onClick = {
                navController.navigate("dashboard") {
                    popUpTo("welcome") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryTextColor,
                contentColor = Color.White
            )
        ) {
            Text("Get Started", fontSize = 16.sp)
        }
    }
}




@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun FintrackPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        WelcomeScreen(navController = navController)
    }
}