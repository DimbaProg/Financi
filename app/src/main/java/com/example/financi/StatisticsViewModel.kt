package com.example.financi


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val expenseCategoryData: List<CategorySumData> = emptyList(),
    val incomeCategoryData: List<CategorySumData> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val selectedPeriod: Period = Period.MONTH,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val lineChartData: LineChartData = LineChartData(emptyList(), emptyList())
)

data class CategorySumData(val categoryName: String, val total: Double)

data class LineChartData(val incomeList: List<Pair<Long, Double>>, val expenseList: List<Pair<Long, Double>>)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    settingsRepo: SettingsRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState

    private val userId: Flow<Long> = settingsRepo.userId

    private val categoryMap = MutableStateFlow<Map<Long, String>>(emptyMap())

    init {
        viewModelScope.launch {
            categoryRepo.getAllCategories().collect { categories ->
                categoryMap.value = categories.associate { it.id to it.name }
            }
        }
        val periodFlow = _uiState.map { it.selectedPeriod }.distinctUntilChanged()
        combine(userId, periodFlow) { id, period ->
            Pair(id, period)
        }.onEach { (id, period) ->
            if (id != -1L) loadStatistics(id, period)
        }.launchIn(viewModelScope)
    }

    fun setPeriod(period: Period) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun setType(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type) }
    }
    val names = categoryMap.value
    private fun loadStatistics(userId: Long, period: Period) {
        val (start, end) = periodToRange(period)
        viewModelScope.launch {
            val income = transactionRepo.getTotalByType(userId, TransactionType.INCOME, start, end).first() ?: 0.0
            val expense = transactionRepo.getTotalByType(userId, TransactionType.EXPENSE, start, end).first() ?: 0.0

            val incomeSums = transactionRepo.getCategorySums(userId, TransactionType.INCOME, start, end).first()
            val expenseSums = transactionRepo.getCategorySums(userId, TransactionType.EXPENSE, start, end).first()

            val incomeData = incomeSums.map { sum ->
                val categoryName = SampleData.categories.find { it.id == sum.categoryId }?.name ?: "Другое"
                CategorySumData(categoryName, sum.total)
            }
            val expenseData = expenseSums.map { sum ->
                val categoryName = SampleData.categories.find { it.id == sum.categoryId }?.name ?: "Другое"
                CategorySumData(categoryName, sum.total)
            }

            val allTransactions = transactionRepo.getTransactionsByDateRange(userId, start, end).first()
            val daysMap = allTransactions.groupBy { normalizeDate(it.date) }
            val daysSorted = daysMap.keys.sorted()
            val incomeLine = daysSorted.map { day ->
                val total = daysMap[day]?.filter { it.type == TransactionType.INCOME }?.sumOf { it.amount } ?: 0.0
                day to total
            }
            val expenseLine = daysSorted.map { day ->
                val total = daysMap[day]?.filter { it.type == TransactionType.EXPENSE }?.sumOf { it.amount } ?: 0.0
                day to total
            }

            _uiState.update {
                it.copy(
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense,
                    expenseCategoryData = expenseData,
                    incomeCategoryData = incomeData,
                    lineChartData = LineChartData(incomeLine, expenseLine)
                )
            }
        }
    }

    private fun normalizeDate(millis: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = millis
            set(java.util.Calendar.HOUR_OF_DAY, 0)
        }
        return cal.timeInMillis
    }

    private fun periodToRange(period: Period): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        val end = cal.timeInMillis
        val start = when (period) {
            Period.WEEK -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            Period.MONTH -> {
                cal.add(java.util.Calendar.MONTH, -1)
                cal.timeInMillis
            }
            Period.YEAR -> {
                cal.add(java.util.Calendar.YEAR, -1)
                cal.timeInMillis
            }
            Period.ALL -> 0L
        }
        return start to end
    }
}