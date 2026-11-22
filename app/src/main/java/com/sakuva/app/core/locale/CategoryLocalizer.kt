package com.sakuva.app.core.locale

/**
 * Maps seeded default category names (stored in Indonesian) to the active app
 * language. User-created custom categories are returned unchanged.
 */
object CategoryLocalizer {

    fun localize(name: String, strings: AppStrings): String = when (name) {
        "Gaji", "Salary" -> strings.categorySalary
        "Bonus" -> strings.categoryBonus
        "Investasi", "Investment" -> strings.categoryInvestment
        "Lainnya", "Lain-lain", "Other" -> strings.categoryOther
        "Makan & Minum", "Makanan & Minuman", "Food & Drinks", "Food" -> strings.categoryFoodDrinks
        "Transportasi", "Transportation", "Transport" -> strings.categoryTransportation
        "Belanja", "Shopping" -> strings.categoryShopping
        "Tagihan", "Bills", "Bills & Utilities" -> strings.categoryBills
        "Kesehatan", "Health" -> strings.categoryHealth
        "Hiburan", "Entertainment" -> strings.categoryEntertainment
        "Pendidikan", "Education" -> strings.categoryEducation
        else -> name
    }
}
