package com.bsoft.inventorymanager.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.bsoft.inventorymanager.models.SaleItem;

/**
 * Utility class for all financial calculations in the application.
 * Ensures strict consistency in pricing, tax, discount, and profit
 * calculations.
 * <p>
 * Rule 1: All currency calculations use BigDecimal for precision, then round to
 * 2 digits.
 * Rule 2: Validation methods return booleans to enforce business rules (e.g.,
 * Selling Price >= Cost Price).
 */
public class FinancialCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculates the total price for a line item (price * quantity).
     *
     * @param pricePerItem The price of a single unit.
     * @param quantity     The quantity being sold/purchased.
     * @return The total price for the item, rounded to 2 decimal places.
     */
    public static double calculateLineItemTotal(double pricePerItem, int quantity) {
        BigDecimal price = BigDecimal.valueOf(pricePerItem);
        BigDecimal qty = BigDecimal.valueOf(quantity);
        return price.multiply(qty).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Calculates the subtotal for a list of sale items.
     *
     * @param items List of sale items.
     * @return The sum of all line item totals.
     */
    public static double calculateSubtotal(List<SaleItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (items != null) {
            for (SaleItem item : items) {
                BigDecimal lineTotal = BigDecimal
                        .valueOf(calculateLineItemTotal(item.getPricePerItem(), item.getQuantity()));
                subtotal = subtotal.add(lineTotal);
            }
        }
        return subtotal.setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Calculates the tax amount based on the subtotal.
     *
     * @param subtotal   The transaction subtotal.
     * @param taxPercent The tax percentage (e.g., 10.0 for 10%).
     * @return The calculated tax amount.
     */
    public static double calculateTaxAmount(double subtotal, double taxPercent) {
        if (taxPercent <= 0)
            return 0.0;
        BigDecimal sub = BigDecimal.valueOf(subtotal);
        BigDecimal taxRate = BigDecimal.valueOf(taxPercent).divide(BigDecimal.valueOf(100), 4, ROUNDING_MODE);
        return sub.multiply(taxRate).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Calculates the discount amount based on the subtotal.
     *
     * @param subtotal        The transaction subtotal.
     * @param discountPercent The discount percentage (e.g., 5.0 for 5%).
     * @return The calculated discount amount.
     */
    public static double calculateDiscountAmount(double subtotal, double discountPercent) {
        if (discountPercent <= 0)
            return 0.0;
        BigDecimal sub = BigDecimal.valueOf(subtotal);
        BigDecimal discRate = BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), 4, ROUNDING_MODE);
        return sub.multiply(discRate).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Calculates the final total amount of a transaction.
     * Formula: Total = Subtotal + Tax - Discount.
     * Note: This strictly follows the existing logic observed in the app.
     * If the business rule changes to (Subtotal - Discount) + Tax, update THIS
     * method.
     *
     * @param subtotal       The subtotal.
     * @param taxAmount      The calculated tax amount.
     * @param discountAmount The calculated discount amount.
     * @return The final total amount.
     */
    public static double calculateTotalAmount(double subtotal, double taxAmount, double discountAmount) {
        BigDecimal sub = BigDecimal.valueOf(subtotal);
        BigDecimal tax = BigDecimal.valueOf(taxAmount);
        BigDecimal disc = BigDecimal.valueOf(discountAmount);

        // Current Logic: Subtotal + Tax - Discount
        return sub.add(tax).subtract(disc).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Validates if a selling price is profitable compared to the cost price.
     *
     * @param sellingPrice The proposed selling price.
     * @param costPrice    The cost price of the product.
     * @return true if sellingPrice >= costPrice, false otherwise.
     */
    public static boolean isValidSellingPrice(double sellingPrice, double costPrice) {
        // We allow selling at cost (0 profit), but not below cost.
        // Using BigDecimal compareTo to avoid double precision issues.
        return BigDecimal.valueOf(sellingPrice).compareTo(BigDecimal.valueOf(costPrice)) >= 0;
    }

    /**
     * Calculates the potential profit margin percentage.
     * Formula: ((Selling Price - Cost Price) / Cost Price) * 100
     *
     * @param sellingPrice The selling price.
     * @param costPrice    The cost price.
     * @return The profit margin percentage, or 0 if cost is 0.
     */
    public static double calculateMarginPercent(double sellingPrice, double costPrice) {
        if (costPrice <= 0)
            return 100.0; // Infinite margin effectively if cost is 0
        BigDecimal sell = BigDecimal.valueOf(sellingPrice);
        BigDecimal cost = BigDecimal.valueOf(costPrice);

        BigDecimal profit = sell.subtract(cost);
        return profit.divide(cost, 4, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Subtracts two doubles accurately using BigDecimal.
     */
    public static double subtract(double v1, double v2) {
        return BigDecimal.valueOf(v1).subtract(BigDecimal.valueOf(v2)).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Adds two doubles accurately using BigDecimal.
     */
    public static double add(double v1, double v2) {
        return BigDecimal.valueOf(v1).add(BigDecimal.valueOf(v2)).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }

    /**
     * Multiplies a double and an int accurately using BigDecimal.
     */
    public static double multiply(double v1, int v2) {
        return BigDecimal.valueOf(v1).multiply(BigDecimal.valueOf(v2)).setScale(SCALE, ROUNDING_MODE).doubleValue();
    }
}
