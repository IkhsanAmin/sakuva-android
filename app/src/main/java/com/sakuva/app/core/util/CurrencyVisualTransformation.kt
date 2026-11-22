package com.sakuva.app.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

private data class CurrencyFormatRules(
    val groupingSeparator: Char,
    val decimalSeparator: Char?,
    val maxFractionDigits: Int,
)

private fun rulesFor(currencyCode: String): CurrencyFormatRules = when (currencyCode.uppercase(Locale.ROOT)) {
    "IDR" -> CurrencyFormatRules(groupingSeparator = '.', decimalSeparator = null, maxFractionDigits = 0)
    "JPY" -> CurrencyFormatRules(groupingSeparator = ',', decimalSeparator = null, maxFractionDigits = 0)
    "EUR" -> CurrencyFormatRules(groupingSeparator = '.', decimalSeparator = ',', maxFractionDigits = 2)
    "USD", "SGD", "MYR" -> CurrencyFormatRules(groupingSeparator = ',', decimalSeparator = '.', maxFractionDigits = 2)
    else -> CurrencyFormatRules(groupingSeparator = ',', decimalSeparator = '.', maxFractionDigits = 2)
}

private val zeroDecimalCurrencies = setOf("IDR", "JPY")

fun isZeroDecimalCurrency(currencyCode: String): Boolean =
    currencyCode.uppercase(Locale.ROOT) in zeroDecimalCurrencies

const val MAX_INTEGER_DIGITS = 13

/**
 * Sanitizes raw user input into the canonical format expected by
 * [com.sakuva.app.core.money.MoneyFormatter.parse].
 *
 * - Zero-decimal currencies (IDR, JPY): digits only, max 13 integer digits.
 * - Decimal currencies: digits + at most one decimal separator (currency-specific),
 *   max 13 integer digits, max 2 fraction digits.
 * - Grouping separators from the visual transformation are stripped.
 * - Leading decimal (`.5`) is normalised to `0.5` / `0,5`.
 */
fun sanitizeAmountInput(raw: String, currencyCode: String): String {
    val code = currencyCode.uppercase(Locale.ROOT)
    val rules = rulesFor(code)

    if (rules.decimalSeparator == null) {
        return raw.filter { it.isDigit() }.take(MAX_INTEGER_DIGITS)
    }

    val decimalSep = rules.decimalSeparator
    val groupingSep = rules.groupingSeparator

    var text = raw

    if (decimalSep == ',') {
        if (text.contains(',')) {
            text = text.replace(".", "")
        } else {
            text = text.replace(".", ",")
        }
    } else {
        text = text.replace(",", "")
    }

    val result = StringBuilder()
    var hasDecimal = false
    var decimalDigits = 0
    var integerDigits = 0

    for (char in text) {
        when {
            char.isDigit() -> {
                if (hasDecimal) {
                    if (decimalDigits < rules.maxFractionDigits) {
                        result.append(char)
                        decimalDigits++
                    }
                } else {
                    if (integerDigits < MAX_INTEGER_DIGITS) {
                        result.append(char)
                        integerDigits++
                    }
                }
            }
            char == decimalSep -> {
                if (!hasDecimal) {
                    hasDecimal = true
                    result.append(decimalSep)
                }
            }
        }
    }

    if (result.isNotEmpty() && result[0] == decimalSep) {
        result.insert(0, '0')
    }

    return result.toString()
}

class CurrencyVisualTransformation(private val currencyCode: String) : VisualTransformation {

    private val rules = rulesFor(currencyCode)

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = StringBuilder()
        val o2t = IntArray(original.length + 1)
        val t2o = mutableListOf<Int>()

        val totalIntegerDigits = countIntegerDigits(original, rules)
        var integerDigits = 0
        var hasDecimal = false

        for (i in original.indices) {
            val char = original[i]
            when {
                char.isDigit() -> {
                    if (!hasDecimal) {
                        if (integerDigits > 0 && (totalIntegerDigits - integerDigits) % 3 == 0) {
                            t2o.add(i)
                            formatted.append(rules.groupingSeparator)
                        }
                        o2t[i] = formatted.length
                        t2o.add(i)
                        formatted.append(char)
                        integerDigits++
                    } else {
                        o2t[i] = formatted.length
                        t2o.add(i)
                        formatted.append(char)
                    }
                }
                rules.decimalSeparator != null && char == rules.decimalSeparator -> {
                    hasDecimal = true
                    o2t[i] = formatted.length
                    t2o.add(i)
                    formatted.append(char)
                }
                else -> {
                    o2t[i] = formatted.length
                }
            }
        }

        o2t[original.length] = formatted.length
        t2o.add(original.length)

        return TransformedText(
            AnnotatedString(formatted.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    o2t[offset.coerceIn(0, o2t.size - 1)]

                override fun transformedToOriginal(offset: Int): Int =
                    t2o[offset.coerceIn(0, t2o.size - 1)]
            },
        )
    }

    private fun countIntegerDigits(s: String, rules: CurrencyFormatRules): Int {
        var count = 0
        for (c in s) {
            if (rules.decimalSeparator != null && c == rules.decimalSeparator) break
            if (c.isDigit()) count++
        }
        return count
    }
}
