package com.sakuva.app.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Single source of truth for money formatting and parsing.
 *
 * Money is always stored as [Long] scaled by 100 (minor units) to keep the
 * arithmetic exact. Formatting/parsing never touches [Double] or [Float].
 */
object MoneyFormatter {

    private val zeroDecimalCurrencies = setOf("IDR", "JPY", "KRW", "VND")

    private val currencyLocales = mapOf(
        "IDR" to Locale.forLanguageTag("id-ID"),
        "USD" to Locale.US,
        "EUR" to Locale.GERMANY,
        "GBP" to Locale.UK,
        "JPY" to Locale.JAPAN,
        "KRW" to Locale.KOREA,
        "VND" to Locale.forLanguageTag("vi-VN"),
        "SGD" to Locale.forLanguageTag("en-SG"),
        "MYR" to Locale.forLanguageTag("ms-MY"),
        "AUD" to Locale.forLanguageTag("en-AU"),
        "CNY" to Locale.forLanguageTag("zh-CN"),
    )

    val supportedCurrencies: List<String> = listOf(
        "IDR", "USD", "EUR", "GBP", "SGD", "MYR", "JPY", "KRW", "VND", "AUD", "CNY",
    )

    fun decimalsFor(currencyCode: String): Int =
        if (currencyCode.uppercase(Locale.ROOT) in zeroDecimalCurrencies) 0 else 2

    fun localeFor(currencyCode: String): Locale =
        currencyLocales[currencyCode.uppercase(Locale.ROOT)] ?: Locale.US

    /**
     * Formats a scaled amount using grouped digits followed by the ISO 4217
     * three-letter code (e.g. `1.234.567 IDR`). Currency symbols are never
     * rendered so the UI stays consistent across locales.
     */
    fun format(
        amountScaled: Long,
        currencyCode: String,
        locale: Locale = localeFor(currencyCode),
    ): String {
        val code = currencyCode.uppercase(Locale.ROOT)
        val decimals = decimalsFor(code)
        val value = BigDecimal.valueOf(amountScaled, 2).setScale(decimals, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }
        return "${formatter.format(value)} $code"
    }

    /** Plain major-unit text (no symbol/grouping) suitable for edit fields. */
    fun toEditableString(amountScaled: Long, currencyCode: String): String {
        val decimals = decimalsFor(currencyCode)
        return BigDecimal.valueOf(amountScaled, 2)
            .setScale(decimals, RoundingMode.HALF_UP)
            .toPlainString()
    }

    /**
     * Raw machine-readable major-unit value for CSV export: no grouping
     * separators and no currency code, only the decimal separator when the
     * currency uses one.
     */
    fun toPlainAmount(amountScaled: Long, currencyCode: String): String {
        val decimals = decimalsFor(currencyCode)
        return BigDecimal.valueOf(amountScaled, 2)
            .setScale(decimals, RoundingMode.HALF_UP)
            .toPlainString()
    }

    /**
     * Parses free-form user input into a scaled [Long].
     *
     * Grouping separators are removed before the decimal separator is
     * normalised to a dot. Returns `null` for blank, malformed, or overflowing
     * input instead of throwing.
     */
    fun parse(input: String, currencyCode: String): Long? {
        val code = currencyCode.uppercase(Locale.ROOT)
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val symbols = DecimalFormatSymbols.getInstance(localeFor(code))
        val withoutGrouping = trimmed.replace(symbols.groupingSeparator.toString(), "")
        val normalised = withoutGrouping
            .replace(symbols.decimalSeparator, '.')
            .filter { it.isDigit() || it == '.' || it == '-' }

        if (normalised.isEmpty() || normalised == "-" || normalised == ".") return null

        val parsed = runCatching { BigDecimal(normalised) }.getOrNull() ?: return null
        val rounded = parsed.setScale(decimalsFor(code), RoundingMode.HALF_UP)
        val scaled = rounded.movePointRight(2)

        return runCatching { scaled.setScale(0, RoundingMode.HALF_UP).longValueExact() }.getOrNull()
    }
}
