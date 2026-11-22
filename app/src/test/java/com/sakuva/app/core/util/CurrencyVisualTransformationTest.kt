package com.sakuva.app.core.util

import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurrencyVisualTransformationTest {

    @Test
    fun sanitizeAmountInput_idr_removesSeparators() {
        assertThat(sanitizeAmountInput("1.250.000", "IDR")).isEqualTo("1250000")
        assertThat(sanitizeAmountInput("1,250,000", "IDR")).isEqualTo("1250000")
    }

    @Test
    fun sanitizeAmountInput_usd_preservesDecimalAndRemovesGrouping() {
        assertThat(sanitizeAmountInput("1,250.50", "USD")).isEqualTo("1250.50")
        assertThat(sanitizeAmountInput("1250.50", "USD")).isEqualTo("1250.50")
    }

    @Test
    fun sanitizeAmountInput_eur_preservesDecimalAndRemovesGrouping() {
        assertThat(sanitizeAmountInput("1.250,50", "EUR")).isEqualTo("1250,50")
        assertThat(sanitizeAmountInput("1250.50", "EUR")).isEqualTo("1250,50")
    }

    @Test
    fun sanitizeAmountInput_limitsTo13Digits() {
        assertThat(sanitizeAmountInput("1234567890123456", "IDR")).isEqualTo("1234567890123")
        assertThat(sanitizeAmountInput("1234567890123456.78", "USD")).isEqualTo("1234567890123.78")
    }

    @Test
    fun sanitizeAmountInput_limitsDecimalDigits() {
        assertThat(sanitizeAmountInput("10.555", "USD")).isEqualTo("10.55")
        assertThat(sanitizeAmountInput("10,555", "EUR")).isEqualTo("10,55")
    }

    @Test
    fun sanitizeAmountInput_normalisesLeadingDecimal() {
        assertThat(sanitizeAmountInput(".5", "USD")).isEqualTo("0.5")
        assertThat(sanitizeAmountInput(",5", "EUR")).isEqualTo("0,5")
    }

    @Test
    fun visualTransformation_idr_formatsCorrectly() {
        val vt = CurrencyVisualTransformation("IDR")
        val result = vt.filter(AnnotatedString("1250000"))
        assertThat(result.text.text).isEqualTo("1.250.000")
    }

    @Test
    fun visualTransformation_usd_formatsCorrectly() {
        val vt = CurrencyVisualTransformation("USD")
        val result = vt.filter(AnnotatedString("1250.5"))
        assertThat(result.text.text).isEqualTo("1,250.5")
    }

    @Test
    fun visualTransformation_eur_formatsCorrectly() {
        val vt = CurrencyVisualTransformation("EUR")
        val result = vt.filter(AnnotatedString("1250,5"))
        assertThat(result.text.text).isEqualTo("1.250,5")
    }

    @Test
    fun offsetMapping_idr_mapsCorrectly() {
        val vt = CurrencyVisualTransformation("IDR")
        val original = "1250"
        val result = vt.filter(AnnotatedString(original))
        val mapping = result.offsetMapping

        // original 1250 -> transformed 1.250
        assertThat(mapping.originalToTransformed(0)).isEqualTo(0)
        assertThat(mapping.originalToTransformed(1)).isEqualTo(2) // after '1' -> after '1.'
        assertThat(mapping.originalToTransformed(2)).isEqualTo(3) // after '12' -> after '1.2'
        assertThat(mapping.originalToTransformed(4)).isEqualTo(5) // after '1250' -> after '1.250'

        assertThat(mapping.transformedToOriginal(0)).isEqualTo(0)
        assertThat(mapping.transformedToOriginal(1)).isEqualTo(1) // after '1' -> after '1'
        assertThat(mapping.transformedToOriginal(2)).isEqualTo(1) // after '1.' -> after '1'
        assertThat(mapping.transformedToOriginal(5)).isEqualTo(4)
    }

    @Test
    fun offsetMapping_usd_withDecimal_mapsCorrectly() {
        val vt = CurrencyVisualTransformation("USD")
        val original = "1250.50"
        val result = vt.filter(AnnotatedString(original))
        val mapping = result.offsetMapping

        // original 1250.50 -> transformed 1,250.50
        assertThat(mapping.originalToTransformed(0)).isEqualTo(0)
        assertThat(mapping.originalToTransformed(1)).isEqualTo(2) // after '1' -> after '1,'
        assertThat(mapping.originalToTransformed(4)).isEqualTo(5) // after '1250' -> after '1,250'
        assertThat(mapping.originalToTransformed(5)).isEqualTo(6) // after '1250.' -> after '1,250.'
        assertThat(mapping.originalToTransformed(7)).isEqualTo(8) // end

        assertThat(mapping.transformedToOriginal(2)).isEqualTo(1) // after '1,' -> after '1'
        assertThat(mapping.transformedToOriginal(6)).isEqualTo(5) // after '1,250.' -> after '1250.'
    }
}
