package com.example.financi


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val filterType: TransactionType? = null,
    val filterPeriod: Period = Period.ALL,
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null
)

enum class Period { WEEK, MONTH, YEAR, ALL }

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState

    private val userId: Flow<Long> = settingsRepo.userId

    private val filterType = MutableStateFlow<TransactionType?>(null)
    private val filterPeriod = MutableStateFlow(Period.ALL)
    private val searchQuery = MutableStateFlow("")
    private val filterCategoryId = MutableStateFlow<Long?>(null)

    init {
        combine(userId, filterType, filterPeriod, searchQuery,filterCategoryId ) { id, type, period, query, categoryId ->
            FilterParams(id, type, period, query, categoryId)
        }.flatMapLatest { params ->
            if (params.userId == -1L) {
                flowOf(emptyList())
            } else {
                _uiState.update { it.copy(isLoading = true) }
                val (start, end) = periodToRange(params.period)
                transactionRepo.getTransactionsByDateRange(params.userId, start, end)
                    .map { list ->
                        var result = list
                        if (params.type != null) {
                            result = result.filter { it.type == params.type }
                        }
                        if (params.query.isNotBlank()) {
                            result = result.filter { it.comment.contains(params.query, ignoreCase = true) }
                        }
                        if (params.categoryId != null) {
                            result = result.filter { it.categoryId == params.categoryId }
                        }
                        result
                    }
            }
        }.onEach { transactions ->
            _uiState.update { it.copy(transactions = transactions, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun setFilterType(type: TransactionType?) {
        filterType.value = type
        _uiState.update { it.copy(filterType = type, selectedCategoryId = null) }
    }

    fun setFilterPeriod(period: Period) {
        filterPeriod.value = period
        _uiState.update { it.copy(filterPeriod = period) }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
    fun setFilterCategory(categoryId: Long?) {
        filterCategoryId.value = categoryId
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepo.delete(transaction)
        }
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

private data class FilterParams(
    val userId: Long,
    val type: TransactionType?,
    val period: Period,
    val query: String,
    val categoryId: Long? = null
)