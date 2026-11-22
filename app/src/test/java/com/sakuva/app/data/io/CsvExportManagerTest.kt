package com.sakuva.app.data.io

import com.google.common.truth.Truth.assertThat
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.model.WalletType
import org.junit.Test
import java.io.ByteArrayOutputStream

class CsvExportManagerTest {

    private val manager = CsvExportManager()

    private fun createWallet(id: Long, name: String) = Wallet(
        id = id,
        name = name,
        type = WalletType.CASH,
        initialBalance = 100000L,
        iconKey = "cash",
        colorHex = "#000000",
        isArchived = false,
        createdAt = 1000L,
    )

    private fun createCategory(id: Long, name: String) = Category(
        id = id,
        name = name,
        type = CategoryType.EXPENSE,
        iconKey = "food",
        colorHex = "#FF0000",
        isDefault = false,
        isArchived = false,
    )

    private fun createTransaction(
        id: Long,
        amount: Long,
        walletId: Long,
        note: String = "",
    ) = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.EXPENSE,
        walletId = walletId,
        toWalletId = null,
        categoryId = 1L,
        dateEpochDay = 19000L,
        note = note,
        createdAt = 1000L,
    )

    @Test
    fun export_generatesValidCsv() {
        val wallets = listOf(createWallet(1L, "Cash"))
        val categories = listOf(createCategory(1L, "Food"))
        val transactions = listOf(createTransaction(1L, 50000L, 1L, "Lunch"))
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, transactions, wallets, categories, "IDR")

        val csv = outputStream.toString(Charsets.UTF_8)
        assertThat(csv).contains("Tanggal")
        assertThat(csv).contains("Tipe")
        assertThat(csv).contains("Nominal")
        assertThat(csv).contains("Lunch")
        assertThat(csv).contains(",500,")
        assertThat(csv).doesNotContain("Rp")
        assertThat(csv).doesNotContain("IDR")
    }

    @Test
    fun export_usesDashForEmptyCells() {
        val wallets = listOf(createWallet(1L, "Cash"))
        val categories = listOf(createCategory(1L, "Food"))
        val transactions = listOf(createTransaction(1L, 50000L, 1L, note = ""))
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, transactions, wallets, categories, "IDR")

        val dataRow = outputStream.toString(Charsets.UTF_8).lines()[1]
        val cells = dataRow.split(",")
        assertThat(cells[4]).isEqualTo("-")
        assertThat(cells[6]).isEqualTo("-")
    }

    @Test
    fun export_sanitizesFormulaInjection() {
        val wallets = listOf(createWallet(1L, "=Malicious"))
        val categories = listOf(createCategory(1L, "Food"))
        val transactions = listOf(createTransaction(1L, 50000L, 1L, "=SUM()"))
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, transactions, wallets, categories, "IDR")

        val csv = outputStream.toString(Charsets.UTF_8)
        assertThat(csv).contains("'=SUM()")
        assertThat(csv).contains("'=Malicious")
    }

    @Test
    fun export_quotesFieldsWithCommas() {
        val wallets = listOf(createWallet(1L, "Bank, Main"))
        val categories = listOf(createCategory(1L, "Food, Drinks"))
        val transactions = listOf(createTransaction(1L, 50000L, 1L, "Note, with, commas"))
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, transactions, wallets, categories, "IDR")

        val csv = outputStream.toString(Charsets.UTF_8)
        assertThat(csv).contains("\"Bank, Main\"")
        assertThat(csv).contains("\"Food, Drinks\"")
        assertThat(csv).contains("\"Note, with, commas\"")
    }

    @Test
    fun export_includesBomForUtf8() {
        val wallets = listOf(createWallet(1L, "Dompet"))
        val categories = listOf(createCategory(1L, "Makanan"))
        val transactions = emptyList<Transaction>()
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, transactions, wallets, categories, "IDR")

        val bytes = outputStream.toByteArray()
        assertThat(bytes.size).isGreaterThan(3)
        assertThat(bytes[0]).isEqualTo(0xEF.toByte())
        assertThat(bytes[1]).isEqualTo(0xBB.toByte())
        assertThat(bytes[2]).isEqualTo(0xBF.toByte())
    }

    @Test
    fun export_handlesEmptyTransactions() {
        val wallets = listOf(createWallet(1L, "Cash"))
        val categories = listOf(createCategory(1L, "Food"))
        val outputStream = ByteArrayOutputStream()

        manager.export(outputStream, emptyList(), wallets, categories, "IDR")

        val csv = outputStream.toString(Charsets.UTF_8)
        assertThat(csv).contains("Tanggal")
        assertThat(csv.lines().size).isEqualTo(2)
    }
}
