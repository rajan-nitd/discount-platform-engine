package com.ecommerce.discount.model;

/**
 * Records a single discount that was applied (or skipped) during calculation.
 */
public record AppliedDiscount(
        String ruleId,
        String description,
        DiscountType type,
        Money discountAmount,
        boolean applied,
        String reason
) {
    public static AppliedDiscount applied(DiscountRule rule, Money amount, String reason) {
        return new AppliedDiscount(rule.getId(), rule.getDescription(), rule.getType(), amount, true, reason);
    }

    public static AppliedDiscount skipped(DiscountRule rule, String reason) {
        return new AppliedDiscount(rule.getId(), rule.getDescription(), rule.getType(), Money.ZERO, false, reason);
    }
}
