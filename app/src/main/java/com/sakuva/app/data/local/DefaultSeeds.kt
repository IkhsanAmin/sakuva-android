package com.sakuva.app.data.local

data class WalletSeed(
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
)

data class CategorySeed(
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
)

object DefaultSeeds {

    val wallets = listOf(
        WalletSeed("Tunai", "CASH", "cash", "#1B6C4F"),
        WalletSeed("BRI", "BANK", "bank", "#2F6690"),
        WalletSeed("BNI", "BANK", "bank", "#E07A3F"),
        WalletSeed("BSI", "BANK", "bank", "#1B8A6B"),
        WalletSeed("BCA", "BANK", "bank", "#3D6373"),
        WalletSeed("E-Wallet", "EWALLET", "ewallet", "#6A4C93"),
    )

    val categories = listOf(
        CategorySeed("Gaji", "INCOME", "salary", "#1B6C4F"),
        CategorySeed("Bonus", "INCOME", "bonus", "#3D6373"),
        CategorySeed("Investasi", "INCOME", "investment", "#6A4C93"),
        CategorySeed("Lainnya", "INCOME", "other", "#8A6D3B"),
        CategorySeed("Makan & Minum", "EXPENSE", "food", "#E07A3F"),
        CategorySeed("Transportasi", "EXPENSE", "transport", "#2F6690"),
        CategorySeed("Belanja", "EXPENSE", "shopping", "#9C4A5F"),
        CategorySeed("Tagihan", "EXPENSE", "bills", "#8A6D3B"),
        CategorySeed("Kesehatan", "EXPENSE", "health", "#1B8A6B"),
        CategorySeed("Hiburan", "EXPENSE", "entertainment", "#6A4C93"),
    )
}
