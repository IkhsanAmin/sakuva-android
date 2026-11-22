package com.sakuva.app.data.repository

import com.sakuva.app.data.local.dao.CategoryDao
import com.sakuva.app.data.mapper.toDomain
import com.sakuva.app.data.mapper.toEntity
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(type: CategoryType?): Flow<List<Category>> =
        categoryDao.observeActiveCategories().map { categories ->
            categories
                .filter { type == null || it.type == type }
                .map { it.toDomain() }
        }

    override fun observeAllCategories(): Flow<List<Category>> =
        categoryDao.observeAllCategories().map { categories -> categories.map { it.toDomain() } }

    override suspend fun getAllCategories(): List<Category> =
        categoryDao.getAll().map { it.toDomain() }

    override suspend fun saveCategory(category: Category): Long {
        val entity = category.toEntity()
        return if (entity.id == 0L) {
            categoryDao.insert(entity)
        } else {
            categoryDao.update(entity)
            entity.id
        }
    }

    override suspend fun archiveCategory(id: Long) = categoryDao.archive(id)
}
