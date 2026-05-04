package com.example.financi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllForUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(userId: Long, start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(userId: Long, categoryId: Long): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getByType(userId: Long, type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :start AND :end")
    fun getTotalByType(
        userId: Long,
        type: TransactionType,
        start: Long,
        end: Long
    ): Flow<Double?>

    data class CategorySum(val categoryId: Long, val total: Double)

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :start AND :end GROUP BY categoryId")
    fun getCategorySums(
        userId: Long,
        type: TransactionType,
        start: Long,
        end: Long
    ): Flow<List<CategorySum>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<TransactionEntity>>
}