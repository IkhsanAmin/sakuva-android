package com.sakuva.app.feature.common

import com.sakuva.app.core.locale.AppStrings

enum class Feedback {
    TRANSACTION_SAVED,
    WALLET_CREATED,
    WALLET_UPDATED,
    WALLET_ARCHIVED,
    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_ARCHIVED,
    BACKUP_SUCCESS,
    RESTORE_SUCCESS,
    EXPORT_SUCCESS,
    TRANSACTION_DELETED,
    DATA_RESET,
    INVALID_BACKUP,
    FILE_TOO_LARGE,
    ERROR,
}

fun AppStrings.messageFor(feedback: Feedback): String = when (feedback) {
    Feedback.TRANSACTION_SAVED -> transactionSaved
    Feedback.WALLET_CREATED -> walletCreated
    Feedback.WALLET_UPDATED -> walletUpdated
    Feedback.WALLET_ARCHIVED -> walletArchived
    Feedback.CATEGORY_CREATED -> categoryCreated
    Feedback.CATEGORY_UPDATED -> categoryUpdated
    Feedback.CATEGORY_ARCHIVED -> categoryArchived
    Feedback.BACKUP_SUCCESS -> backupSuccess
    Feedback.RESTORE_SUCCESS -> restoreSuccess
    Feedback.EXPORT_SUCCESS -> exportSuccess
    Feedback.TRANSACTION_DELETED -> transactionDeleted
    Feedback.DATA_RESET -> dataResetSuccess
    Feedback.INVALID_BACKUP -> invalidBackupFile
    Feedback.FILE_TOO_LARGE -> fileTooLarge
    Feedback.ERROR -> genericError
}
