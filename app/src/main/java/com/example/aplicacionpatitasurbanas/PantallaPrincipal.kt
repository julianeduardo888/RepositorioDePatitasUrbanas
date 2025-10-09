package com.example.aplicacionpatitasurbanas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.aplicacionpatitasurbanas.R
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles

@Composable
fun PantallaPrincipal(navController: NavHostController) {  // 👈 recibe el controlador
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Patitas Urbanas",
                style = TextStyle(
                    fontFamily = RubikPuddles,
                    fontSize = 40.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.ellipse_1),
                contentDescription = "Logo Patitas Urbanas",
                modifier = Modifier
                    .size(324.dp)
                    .clickable { navController.navigate("pantalla2") }, // 👈 navega
                contentScale = ContentScale.Fit
            )
        }
    }
}
