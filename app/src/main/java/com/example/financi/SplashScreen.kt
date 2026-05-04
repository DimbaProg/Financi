package com.example.financi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    val viewModel: SplashViewModel = hiltViewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        delay(1000)
        if (isLoggedIn) {
            onNavigateToMain()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "FinanceApp", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        if (!isLoggedIn) {
            Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Войти")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick =    onNavigateToRegistration ,
                modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Зарегистрироваться")
            }
        } else {
            CircularProgressIndicator()
        }
    }
}