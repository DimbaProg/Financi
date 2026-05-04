package com.example.financi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val splashViewModel: SplashViewModel = hiltViewModel()
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsState(initial = false)

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToMain = { navController.navigate("main") { popUpTo("splash") { inclusive = true } } },
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegistration={navController.navigate("registration")}
            )
        }
        composable("login") {
            LoginScreen(
                onSuccess = { navController.navigate("main") { popUpTo(0) } },
                onRegisterClick = { navController.navigate("registration") }
            )
        }
        composable("registration") {
            RegistrationScreen(
                onSuccess = { navController.navigate("main") { popUpTo(0) } },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("main") {
            MainScreen(
                onAddTransaction = { navController.navigate("add_transaction") },
                onViewAll = { navController.navigate("transaction_list")},
                onSettings = { navController.navigate("settings")},
                onStatistics = { navController.navigate("statistics") }
            )
        }
        composable("add_transaction") {
            AddTransactionScreen(onSaved = { navController.popBackStack() })
        }
        composable("transaction_list") {
            TransactionListScreen(
                onEditTransaction = { transaction ->
                    navController.navigate("edit_transaction/${transaction.id}")
                }
            )
        }
        composable(
            "edit_transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId")
            AddTransactionScreen(
                transactionId = transactionId,
                onSaved = { navController.popBackStack() }
            )
        }
        composable("statistics") {
            StatisticsScreen()
        }
        composable("settings") {
            SettingsScreen(
                onLogout = {
                    splashViewModel.logout()
                    navController.navigate("splash") { popUpTo(0) }
                }
            )
        }
    }
}