package com.sakuva.app.core.money

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class MoneyFormatterTest {

    @Test
    fun format_idr_scalesBy100AndUsesIsoCode() {
        // 10000 scaled = 100.00 IDR
        val formatted = MoneyFormatter.format(10000L, "IDR", Locale.US)
        assertThat(formatted).isEqualTo("100 IDR")
        assertThat(formatted).doesNotContain("Rp")
        assertThat(formatted).doesNotContain("$")
    }

    @Test
    fun format_usd_usesDecimalAndIsoCode() {
        val formatted = MoneyFormatter.format(123456L, "USD", Locale.US)
        assertThat(formatted).isEqualTo("1,234.56 USD")
    }

    @Test
    fun parse_validIdrAmount_returnsScaledLong() {
        val parsed = MoneyFormatter.parse("100", "IDR")
        assertThat(parsed).isEqualTo(10000L)
    }

    @Test
    fun parse_invalidString_returnsNull() {
        val parsed = MoneyFormatter.parse("abc", "IDR")
        assertThat(parsed).isNull()
    }

    @Test
    fun toEditableString_formatsWithoutSymbol() {
        val editable = MoneyFormatter.toEditableString(10000L, "IDR")
        assertThat(editable).isEqualTo("100")
    }

    @Test
    fun toPlainAmount_hasNoGroupingOrSymbol() {
        assertThat(MoneyFormatter.toPlainAmount(123456700L, "IDR")).isEqualTo("1234567")
        assertThat(MoneyFormatter.toPlainAmount(123456L, "USD")).isEqualTo("1234.56")
    }
}
