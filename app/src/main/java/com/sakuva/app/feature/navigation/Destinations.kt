package com.sakuva.app.feature.navigation

import kotlinx.serialization.Serializable

@Serializable
object DashboardDestination

@Serializable
object HistoryDestination

@Serializable
object WalletDestination

@Serializable
object SettingsDestination

@Serializable
object CategoryManagementDestination

@Serializable
data class TransactionEditorDestination(
    val transactionId: Long? = null,
    val duplicateId: Long? = null,
)
