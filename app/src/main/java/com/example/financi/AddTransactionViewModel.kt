package com.example.financi


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    settingsRepo: SettingsRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state

    init {
        viewModelScope.launch {
            loadCategoriesWithDefaults()
            settingsRepo.userId.collect { userId ->
                if (userId != -1L) {
                    categoryRepo.getCategoriesByUser(userId).collect { categories ->
                        _state.update { it.copy(userId = userId) }

                    }
                }

                viewModelScope.launch {
                    categoryRepo.getAllCategories().collect { categories ->
                        _state.update { it.copy(categories = categories) }
                    }
                }
            }
        }
    }
    private fun loadCategoriesWithDefaults() {
        viewModelScope.launch {
            val currentCategories = categoryRepo.getAllCategories().first()
            if (currentCategories.isEmpty()) {
                listOf("Еда", "Транспорт", "Зарплата", "Развлечения").forEach { name ->
                    categoryRepo.insert(CategoryEntity(name = name))
                }
            }
            categoryRepo.getAllCategories().collect { entities ->
                _state.update { state ->
                    state.copy(
                        categories = entities.map { CategoryEntity(it.id, it.name) }
                    )
                }
            }
        }
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepo.getTransactionById(transactionId)
            if (transaction != null) {
                _state.update {
                    it.copy(
                        transactionId = transaction.id,
                        amount = transaction.amount.toString(),
                        type = transaction.type,
                        categoryId = transaction.categoryId,
                        date = transaction.date,
                        comment = transaction.comment
                    )
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' || it == ',' }
        _state.update { it.copy(amount = filtered) }
        _state.update { it.copy(amount = amount) } }
    fun onTypeSelected(type: TransactionType) { _state.update { it.copy(type = type) } }
    fun onCategorySelected(categoryId: Long) { _state.update { it.copy(categoryId = categoryId) } }
    fun onDateChange(millis: Long) { _state.update { it.copy(date = millis) } }
    fun onCommentChange(comment: String) { _state.update { it.copy(comment = comment) } }

    fun saveTransaction() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        viewModelScope.launch {
            if (s.transactionId != null) {
                transactionRepo.update(
                    TransactionEntity(
                        id = s.transactionId,
                        userId = s.userId,
                        amount = amount,
                        type = s.type,
                        categoryId = s.categoryId,
                        date = s.date,
                        comment = s.comment
                    )
                )
            } else {
                transactionRepo.insert(
                    TransactionEntity(
                        userId = s.userId,
                        amount = amount,
                        type = s.type,
                        categoryId = s.categoryId,
                        date = s.date,
                        comment = s.comment
                    )
                )
            }
            _state.update { AddTransactionState(userId = s.userId) }
        }
    }
}

data class AddTransactionState(
    val transactionId: Long? = null,
    val userId: Long = 0,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<CategoryEntity> = emptyList() ,
    val categoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val comment: String = ""
)