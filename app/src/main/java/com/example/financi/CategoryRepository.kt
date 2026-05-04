package com.example.financi

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getCategoriesByUser(userId: Long): Flow<List<CategoryEntity>>
    suspend fun insert(category: CategoryEntity)
    suspend fun update(category: CategoryEntity)
    suspend fun delete(category: CategoryEntity)
}
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override fun getCategoriesByUser(userId: Long): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByUser(userId)

    override suspend fun insert(category: CategoryEntity) =
        categoryDao.insert(category)

    override suspend fun update(category: CategoryEntity) =
        categoryDao.update(category)

    override suspend fun delete(category: CategoryEntity) =
        categoryDao.delete(category)
}