package com.sakuva.app.data.io

import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.Wallet
import java.io.OutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RFC 4180 CSV writer hardened against formula injection. The output always
 * starts with a UTF-8 BOM so Excel and Google Sheets detect the encoding.
 */
@Singleton
class CsvExportManager @Inject constructor() {

    fun buildCsvString(
        transactions: List<Transaction>,
        wallets: List<Wallet>,
        categories: List<Category>,
        currencyCode: String,
        locale: Locale = MoneyFormatter.localeFor(currencyCode),
    ): String {
        val walletNames = wallets.associate { it.id to it.name }
        val categoryNames = categories.associate { it.id to it.name }

        val builder = StringBuilder()
        builder.append(BOM)
        builder.append(HEADER)
        builder.append(ROW_SEPARATOR)

        transactions
            .sortedWith(compareByDescending<Transaction> { it.dateEpochDay }.thenByDescending { it.createdAt })
            .forEach { transaction ->
                val cells = listOf(
                    DateTimeUtils.formatEpochDay(transaction.dateEpochDay, "yyyy-MM-dd", locale),
                    typeLabel(transaction.type),
                    transaction.categoryId?.let { categoryNames[it] }.orDash(),
                    walletNames[transaction.walletId].orDash(),
                    transaction.toWalletId?.let { walletNames[it] }.orDash(),
                    MoneyFormatter.toPlainAmount(transaction.amount, currencyCode),
                    transaction.note.orDash(),
                )
                builder.append(cells.joinToString(separator = ",", transform = ::escapeField))
                builder.append(ROW_SEPARATOR)
            }

        return builder.toString()
    }

    fun export(
        outputStream: OutputStream,
        transactions: List<Transaction>,
        wallets: List<Wallet>,
        categories: List<Category>,
        currencyCode: String,
        locale: Locale = MoneyFormatter.localeFor(currencyCode),
    ) {
        val csv = buildCsvString(transactions, wallets, categories, currencyCode, locale)
        outputStream.write(csv.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    fun escapeField(value: String): String {
        val sanitized = if (value == EMPTY_PLACEHOLDER) value else sanitizeFormula(value)
        val escaped = sanitized.replace("\"", "\"\"")
        val needsQuoting = escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) "\"$escaped\"" else escaped
    }

    fun sanitizeFormula(value: String): String {
        if (value.isEmpty()) return value
        return if (value[0] in FORMULA_PREFIXES) "'$value" else value
    }

    private fun typeLabel(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> "Pemasukan"
        TransactionType.EXPENSE -> "Pengeluaran"
        TransactionType.TRANSFER -> "Transfer"
    }

    private fun String?.orDash(): String = if (this.isNullOrBlank()) EMPTY_PLACEHOLDER else this

    private companion object {
        const val BOM = "\uFEFF"
        const val EMPTY_PLACEHOLDER = "-"
        const val HEADER = "Tanggal,Tipe,Kategori,Sumber Dana,Dompet Tujuan,Nominal,Catatan"
        const val ROW_SEPARATOR = "\r\n"
        val FORMULA_PREFIXES = charArrayOf('=', '+', '-', '@', '\t', '\r')
    }
}
