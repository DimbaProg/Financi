package com.example.financi


import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getAllForUser(userId)

    fun getTransactionsByDateRange(userId: Long, start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getByDateRange(userId, start, end)

    fun getRecentTransactions(userId: Long, limit: Int = 5): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    fun getCategorySums(userId: Long, type: TransactionType, start: Long, end: Long) =
        transactionDao.getCategorySums(userId, type, start, end)

    fun getTotalByType(userId: Long, type: TransactionType, start: Long, end: Long): Flow<Double?> =
        transactionDao.getTotalByType(userId, type, start, end)
    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getById(id)
    }

    suspend fun insert(transaction: TransactionEntity) = transactionDao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

}