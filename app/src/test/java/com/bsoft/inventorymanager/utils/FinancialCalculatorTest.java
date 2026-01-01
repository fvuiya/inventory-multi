package com.bsoft.inventorymanager.utils;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class FinancialCalculatorTest {

    @Test
    public void calculateLineItemTotal_correctlyMultipliesAndRounds() {
        // 10.50 * 3 = 31.50
        assertThat(FinancialCalculator.calculateLineItemTotal(10.50, 3)).isEqualTo(31.50);

        // 10.555 * 2 = 21.11 (RoundingMode.HALF_UP)
        assertThat(FinancialCalculator.calculateLineItemTotal(10.555, 2)).isEqualTo(21.11);
    }

    @Test
    public void calculateTaxAmount_correctlyCalculatesPercentage() {
        // 100 * 10% = 10.00
        assertThat(FinancialCalculator.calculateTaxAmount(100.0, 10.0)).isEqualTo(10.00);

        // 50 * 5.5% = 2.75
        assertThat(FinancialCalculator.calculateTaxAmount(50.0, 5.5)).isEqualTo(2.75);

        // 0 tax
        assertThat(FinancialCalculator.calculateTaxAmount(100.0, 0)).isEqualTo(0.00);
    }

    @Test
    public void calculateDiscountAmount_correctlyCalculatesPercentage() {
        // 100 * 10% = 10.00
        assertThat(FinancialCalculator.calculateDiscountAmount(100.0, 10.0)).isEqualTo(10.00);

        // 0 discount
        assertThat(FinancialCalculator.calculateDiscountAmount(100.0, 0)).isEqualTo(0.00);
    }

    @Test
    public void calculateTotalAmount_appliesFormulaCorrectly() {
        // Total = Subtotal + Tax - Discount
        double subtotal = 100.0;
        double tax = 10.0;
        double discount = 5.0;

        // 100 + 10 - 5 = 105
        assertThat(FinancialCalculator.calculateTotalAmount(subtotal, tax, discount)).isEqualTo(105.00);

        // 100 + 10 - 0 = 110
        assertThat(FinancialCalculator.calculateTotalAmount(subtotal, tax, 0)).isEqualTo(110.00);
    }

    @Test
    public void isValidSellingPrice_enforcesCostLimit() {
        // Selling > Cost -> Valid
        assertThat(FinancialCalculator.isValidSellingPrice(110.0, 100.0)).isTrue();

        // Selling == Cost -> Valid
        assertThat(FinancialCalculator.isValidSellingPrice(100.0, 100.0)).isTrue();

        // Selling < Cost -> Invalid
        assertThat(FinancialCalculator.isValidSellingPrice(99.99, 100.0)).isFalse();
    }

    @Test
    public void calculateMarginPercent_calculatesCorrectly() {
        // (150 - 100) / 100 = 50%
        assertThat(FinancialCalculator.calculateMarginPercent(150.0, 100.0)).isEqualTo(50.00);

        // (100 - 100) / 100 = 0%
        assertThat(FinancialCalculator.calculateMarginPercent(100.0, 100.0)).isEqualTo(0.00);

        // Cost 0 -> 100% (Edge Case handling)
        assertThat(FinancialCalculator.calculateMarginPercent(100.0, 0.0)).isEqualTo(100.00);
    }
}
