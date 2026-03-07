package com.ecommerce.discount.model;

import java.util.List;

/**
 * The output of the discount calculation engine.
 */
public record DiscountResult(
        Money originalPrice,
        Money finalPrice,
        Money totalDiscount,
        List<AppliedDiscount> appliedDiscounts,
        String reasoning
) {
}
