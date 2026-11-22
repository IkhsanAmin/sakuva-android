package com.sakuva.app.domain.repository

import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(type: CategoryType? = null): Flow<List<Category>>
    fun observeAllCategories(): Flow<List<Category>>
    suspend fun getAllCategories(): List<Category>
    suspend fun saveCategory(category: Category): Long
    suspend fun archiveCategory(id: Long)
}
