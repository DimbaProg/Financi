package com.example.financi


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddTransaction: () -> Unit,
    onViewAll: () -> Unit,
    onSettings: () -> Unit,
    onStatistics: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
)

{
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val transactionsWithCategory = remember(recentTransactions) {
        recentTransactions.map { transaction ->
            val categoryName = SampleData.categories.find { it.id == transaction.categoryId }?.name ?: "Другое"
            Pair(transaction, categoryName)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Финансы") },
                actions = {
                    IconButton(onClick = onStatistics) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Статистика"
                        )
                    }
                    IconButton(onClick = onSettings  ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Баланс за текущий месяц", style = MaterialTheme.typography.titleMedium)
                        Text("%.2f ₽".format(balance), style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
            item {
                Text("Последние операции", style = MaterialTheme.typography.titleMedium)
            }
            items(transactionsWithCategory, key = { it.first.id }) { (transaction, categoryName) ->
                TransactionItem(transaction, categoryName)
            }
            item {
                TextButton(onClick = onViewAll) {
                    Text("Показать все")
                }
            }
        }
    }
}



@Composable
fun TransactionItem(transaction: TransactionEntity, categoryName: String) {
    val dateFormat = remember {
        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top)
        {
            Column {
                Text(transaction.comment.ifEmpty { "Без комментария" })
                Text(dateFormat.format(java.util.Date(transaction.date)), style = MaterialTheme.typography.bodySmall)

            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}%.2f".format(transaction.amount),
            color = if (transaction.type == TransactionType.INCOME)
                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 15.dp)
        )
    }
    }
}
