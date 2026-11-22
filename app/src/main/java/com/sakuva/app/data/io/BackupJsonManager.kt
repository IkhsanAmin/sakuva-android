package com.sakuva.app.data.io

import com.sakuva.app.data.local.SakuvaDatabase
import com.sakuva.app.data.local.entity.CategoryEntity
import com.sakuva.app.data.local.entity.TransactionEntity
import com.sakuva.app.data.local.entity.WalletEntity
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.WalletType
import androidx.room.withTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

enum class BackupError {
    FILE_TOO_LARGE,
    INVALID_FORMAT,
    INVALID_SCHEMA,
    EMPTY_WALLETS,
    DUPLICATE_OR_INVALID_ID,
    INVALID_WALLET,
    INVALID_CATEGORY,
    INVALID_TRANSACTION,
}

class BackupException(
    val error: BackupError,
) : Exception(error.name)

/**
 * Reads/writes the user-facing `.json` backup. Everything coming from SAF is
 * treated as hostile: bounded read, strict parser, full domain validation,
 * then a single atomic replace transaction.
 */
@Singleton
class BackupJsonManager @Inject constructor(
    private val database: SakuvaDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
        encodeDefaults = true
    }

    suspend fun export(outputStream: OutputStream) {
        val payload = SakuvaBackupPayload(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            wallets = database.walletDao().getAll().map { it.toBackupDto() },
            categories = database.categoryDao().getAll().map { it.toBackupDto() },
            transactions = database.transactionDao().getAll().map { it.toBackupDto() },
        )
        outputStream.write(json.encodeToString(payload).toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    suspend fun restore(inputStream: InputStream) {
        val bytes = readBounded(inputStream)
        val payload = decodeStrict(bytes)
        validate(payload)
        replaceAll(payload)
    }

    private fun decodeStrict(bytes: ByteArray): SakuvaBackupPayload = try {
        json.decodeFromString(SakuvaBackupPayload.serializer(), bytes.decodeToString())
    } catch (_: Exception) {
        throw BackupException(BackupError.INVALID_FORMAT)
    }

    private suspend fun replaceAll(payload: SakuvaBackupPayload) {
        database.withTransaction {
            val transactionDao = database.transactionDao()
            transactionDao.deleteAll()
            database.categoryDao().deleteAll()
            database.walletDao().deleteAll()

            database.walletDao().insertAll(payload.wallets.map { it.toEntity() })
            database.categoryDao().insertAll(payload.categories.map { it.toEntity() })
            transactionDao.insertAll(payload.transactions.map { it.toEntity() })
        }
    }

    private fun readBounded(inputStream: InputStream): ByteArray {
        val buffer = ByteArray(READ_BUFFER_BYTES)
        val output = ByteArrayOutputStream()
        var totalBytes = 0L

        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) break
            totalBytes += read
            if (totalBytes > MAX_FILE_BYTES) {
                throw BackupException(BackupError.FILE_TOO_LARGE)
            }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        const val MAX_FILE_BYTES = 15L * 1024L * 1024L
        private const val READ_BUFFER_BYTES = 8 * 1024

        private val colorRegex = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
        private val walletTypes = WalletType.entries.map { it.name }.toSet()
        private val categoryTypes = CategoryType.entries.map { it.name }.toSet()
        private val transactionTypes = TransactionType.entries.map { it.name }.toSet()

        fun ensureWithinSizeLimit(size: Long) {
            if (size > MAX_FILE_BYTES) throw BackupException(BackupError.FILE_TOO_LARGE)
        }

        fun validate(payload: SakuvaBackupPayload) {
            if (payload.schemaVersion !in 1..CURRENT_SCHEMA_VERSION) {
                throw BackupException(BackupError.INVALID_SCHEMA)
            }
            if (payload.wallets.isEmpty()) {
                throw BackupException(BackupError.EMPTY_WALLETS)
            }

            ensureUniquePositiveIds(payload.wallets.map { it.id })
            ensureUniquePositiveIds(payload.categories.map { it.id })
            ensureUniquePositiveIds(payload.transactions.map { it.id })

            payload.wallets.forEach { wallet -> validateWallet(wallet) }
            payload.categories.forEach { category -> validateCategory(category) }

            val walletIds = payload.wallets.map { it.id }.toSet()
            val categoryIds = payload.categories.map { it.id }.toSet()
            payload.transactions.forEach { transaction ->
                validateTransaction(transaction, walletIds, categoryIds)
            }
        }

        private fun ensureUniquePositiveIds(ids: List<Long>) {
            if (ids.any { it <= 0L } || ids.size != ids.toSet().size) {
                throw BackupException(BackupError.DUPLICATE_OR_INVALID_ID)
            }
        }

        private fun validateWallet(wallet: WalletBackupDto) {
            if (wallet.name.isBlank() || wallet.iconKey.isBlank()) {
                throw BackupException(BackupError.INVALID_WALLET)
            }
            if (!colorRegex.matches(wallet.colorHex)) {
                throw BackupException(BackupError.INVALID_WALLET)
            }
            if (wallet.type !in walletTypes) {
                throw BackupException(BackupError.INVALID_WALLET)
            }
        }

        private fun validateCategory(category: CategoryBackupDto) {
            if (category.name.isBlank() || category.iconKey.isBlank()) {
                throw BackupException(BackupError.INVALID_CATEGORY)
            }
            if (!colorRegex.matches(category.colorHex)) {
                throw BackupException(BackupError.INVALID_CATEGORY)
            }
            if (category.type !in categoryTypes) {
                throw BackupException(BackupError.INVALID_CATEGORY)
            }
        }

        private fun validateTransaction(
            transaction: TransactionBackupDto,
            walletIds: Set<Long>,
            categoryIds: Set<Long>,
        ) {
            if (transaction.amount <= 0L) {
                throw BackupException(BackupError.INVALID_TRANSACTION)
            }
            if (transaction.type !in transactionTypes) {
                throw BackupException(BackupError.INVALID_TRANSACTION)
            }
            if (transaction.walletId !in walletIds) {
                throw BackupException(BackupError.INVALID_TRANSACTION)
            }

            when (transaction.type) {
                TransactionType.TRANSFER.name -> {
                    val targetWalletId = transaction.toWalletId
                    if (targetWalletId == null || targetWalletId == transaction.walletId) {
                        throw BackupException(BackupError.INVALID_TRANSACTION)
                    }
                    if (targetWalletId !in walletIds || transaction.categoryId != null) {
                        throw BackupException(BackupError.INVALID_TRANSACTION)
                    }
                }

                TransactionType.INCOME.name, TransactionType.EXPENSE.name -> {
                    val categoryId = transaction.categoryId
                    if (categoryId == null || categoryId !in categoryIds) {
                        throw BackupException(BackupError.INVALID_TRANSACTION)
                    }
                    if (transaction.toWalletId != null) {
                        throw BackupException(BackupError.INVALID_TRANSACTION)
                    }
                }
            }
        }
    }
}

private fun WalletEntity.toBackupDto() = WalletBackupDto(
    id = id,
    name = name,
    type = type.name,
    initialBalance = initialBalance,
    iconKey = iconKey,
    colorHex = colorHex,
    isArchived = isArchived,
    createdAt = createdAt,
)

private fun CategoryEntity.toBackupDto() = CategoryBackupDto(
    id = id,
    name = name,
    type = type.name,
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    isArchived = isArchived,
)

private fun TransactionEntity.toBackupDto() = TransactionBackupDto(
    id = id,
    amount = amount,
    type = type.name,
    walletId = walletId,
    toWalletId = toWalletId,
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    createdAt = createdAt,
)

private fun WalletBackupDto.toEntity() = WalletEntity(
    id = id,
    name = name,
    type = WalletType.valueOf(type),
    initialBalance = initialBalance,
    iconKey = iconKey,
    colorHex = colorHex,
    isArchived = isArchived,
    createdAt = createdAt,
)

private fun CategoryBackupDto.toEntity() = CategoryEntity(
    id = id,
    name = name,
    type = CategoryType.valueOf(type),
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    isArchived = isArchived,
)

private fun TransactionBackupDto.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    walletId = walletId,
    toWalletId = toWalletId,
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    createdAt = createdAt,
)
