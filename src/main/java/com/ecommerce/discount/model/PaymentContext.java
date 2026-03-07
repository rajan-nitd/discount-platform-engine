package com.ecommerce.discount.model;

/**
 * Payment information used to evaluate payment-based discounts.
 */
public class PaymentContext {

    private String paymentMethod;
    private String issuer;

    public PaymentContext(String paymentMethod, String issuer) {
        this.paymentMethod = paymentMethod;
        this.issuer = issuer;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public String getIssuer() { return issuer; }
}
