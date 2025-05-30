package com.example.fintrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen (navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
         Column (
             modifier = Modifier
                 .fillMaxSize()
                 .padding(24.dp),
             verticalArrangement = Arrangement.SpaceEvenly,
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Image(
                 painter = painterResource(id = R.drawable.smartphone),
                 contentDescription = "Fintrack Logo",
                 modifier = Modifier.size(150.dp)
             )

             Text(
                 text = "Selamat Datang di Fintrack!",
                 fontSize = 26.sp,
                 fontWeight = FontWeight.Bold
             )

             Text(
                 text = "Kelola keuanganmu dengan mudah dan cerdas.",
                 fontSize = 16.sp,
                 color = Color.Gray
             )

             Button(
                 onClick = {
                     navController.navigate("dashboard") {
                         popUpTo("welcome") { inclusive = true } // mencegah kembali ke welcome screen
                     }
                 },
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(50.dp)
             ) {
                 Text(text = "Get Started", fontSize = 18.sp)
             }
         }
    }
}