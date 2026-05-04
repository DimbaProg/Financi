package com.example.financi


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onEditTransaction: (TransactionEntity) -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Транзакции") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("Поиск по комментарию") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filteredCategories = when (state.filterType) {
                    TransactionType.INCOME -> SampleData.categories.filter { it.name == "Зарплата" }
                    TransactionType.EXPENSE -> SampleData.categories.filter { it.name != "Зарплата" }
                    null -> SampleData.categories
                }
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = state.filterType == TransactionType.INCOME,
                    onClick = { viewModel.setFilterType(TransactionType.INCOME) },
                    label = { Text("Доходы") }
                )
                FilterChip(
                    selected = state.filterType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilterType(TransactionType.EXPENSE) },
                    label = { Text("Расходы") }

                )
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.setFilterCategory(null) },
                    label = { Text("Все категории") }
                )

            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp))
            {
                SampleData.categories.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.setFilterCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Period.values().forEach { period ->
                    FilterChip(
                        selected = state.filterPeriod == period,
                        onClick = { viewModel.setFilterPeriod(period) },
                        label = {
                            Text(
                                when (period) {
                                    Period.WEEK -> "Неделя"
                                    Period.MONTH -> "Месяц"
                                    Period.YEAR -> "Год"
                                    Period.ALL -> "Всё"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.transactions, key = { it.id }) { transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) },
                            onEdit = { onEditTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                    SwipeToDismissBoxValue.StartToEnd -> Color.Blue
                    else -> Color.Transparent
                },
                label = "bg"
            )
            val contentAlignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = contentAlignment
            ) {
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Text("Удалить", color = Color.White)
                    SwipeToDismissBoxValue.StartToEnd -> Text("Редактировать", color = Color.White)
                    else -> {}
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TransactionItemContent(transaction)
            }
        }
    )
}

@Composable
fun TransactionItemContent(transaction: TransactionEntity) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val categoryName = remember(transaction.categoryId) {
        SampleData.categories.find { it.id == transaction.categoryId }?.name ?: "Другое"
    }
    Row(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.comment.ifEmpty { "Без комментария" })
            Text(
                text = dateFormat.format(Date(transaction.date)),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}%.2f".format(transaction.amount),
            color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336),
            style = MaterialTheme.typography.titleMedium
        )
    }
}