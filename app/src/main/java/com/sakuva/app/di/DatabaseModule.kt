package com.sakuva.app.di

import android.content.Context
import androidx.room.Room
import com.sakuva.app.data.local.DatabaseCallback
import com.sakuva.app.data.local.SakuvaDatabase
import com.sakuva.app.data.local.dao.CategoryDao
import com.sakuva.app.data.local.dao.TransactionDao
import com.sakuva.app.data.local.dao.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SakuvaDatabase =
        Room.databaseBuilder(context, SakuvaDatabase::class.java, SakuvaDatabase.NAME)
            .addCallback(DatabaseCallback())
            .build()

    @Provides
    fun provideWalletDao(database: SakuvaDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideCategoryDao(database: SakuvaDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: SakuvaDatabase): TransactionDao = database.transactionDao()
}
