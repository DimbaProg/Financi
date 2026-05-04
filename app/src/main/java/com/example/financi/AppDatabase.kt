package com.example.financi

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}