package com.ecommerce.discount.model;

/**
 * Enum defining the supported discount types.
 * The ordinal implicitly defines default application priority (lower = applied first).
 */
public enum DiscountType {
    BRAND,          // Applied first — item-level, brand-specific
    CATEGORY,       // Applied second — item-level, category-specific
    VOUCHER,        // Applied third — cart-level coupon code
    PAYMENT_OFFER   // Applied last — payment-method-specific
}
