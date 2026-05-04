package com.example.financi



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val userId = withContext(Dispatchers.IO) {
                authRepo.getCurrentUserId()
            }
            if (userId != -1L) {
                initDefaultCategories(userId)
            }
        }
    }

    private suspend fun initDefaultCategories(userId: Long) {
        val existing = categoryRepo.getCategoriesByUser(userId).first()
        if (existing.isEmpty()) {
            listOf("Еда", "Транспорт", "Зарплата", "Развлечения").forEach { name ->
                categoryRepo.insert(CategoryEntity(name = name, userId = userId))
            }
        }
    }

    private val currentUserId: Long
        get() = runBlocking { authRepo.getCurrentUserId() }

    val balance: StateFlow<Double> = transactionRepo
        .getTotalByType(currentUserId, TransactionType.INCOME, startOfMonth(), endOfMonth())
        .combine(
            transactionRepo.getTotalByType(currentUserId, TransactionType.EXPENSE, startOfMonth(), endOfMonth())
        ) { income, expense ->
            (income ?: 0.0) - (expense ?: 0.0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentTransactions: StateFlow<List<TransactionEntity>> = transactionRepo
        .getRecentTransactions(currentUserId, 5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategorySums = transactionRepo
        .getCategorySums(currentUserId, TransactionType.EXPENSE, startOfMonth(), endOfMonth())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


}
private fun startOfMonth(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
    }
    return cal.timeInMillis
}

private fun endOfMonth(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}
