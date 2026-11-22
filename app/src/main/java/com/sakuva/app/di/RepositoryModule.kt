package com.sakuva.app.di

import com.sakuva.app.data.local.preferences.SettingsDataStore
import com.sakuva.app.data.repository.CategoryRepositoryImpl
import com.sakuva.app.data.repository.TransactionRepositoryImpl
import com.sakuva.app.data.repository.WalletRepositoryImpl
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.TransactionRepository
import com.sakuva.app.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository
}
