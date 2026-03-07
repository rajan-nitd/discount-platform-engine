package com.ecommerce.discount.model;

/**
 * Payment information used to evaluate payment-based discounts.
 */
public record PaymentContext(
        String paymentMethod,   // e.g., "CREDIT_CARD"
        String issuer           // e.g., "ICICI", "HDFC"
) {
}
