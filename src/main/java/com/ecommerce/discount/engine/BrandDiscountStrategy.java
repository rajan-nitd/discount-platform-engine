package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

public class BrandDiscountStrategy implements DiscountStrategy {

    @Override
    public AppliedDiscount apply(DiscountRule rule, Cart cart, Money runningTotal) {
        Money eligibleTotal = Money.ZERO;
        for (CartItem item : cart.getItems()) {
            if (item.getBrand().equalsIgnoreCase(rule.getTargetBrand())) {
                eligibleTotal = eligibleTotal.add(item.lineTotal());
            }
        }

        if (eligibleTotal.isZero()) {
            return AppliedDiscount.skipped(rule, "No items from brand '" + rule.getTargetBrand() + "'");
        }

        Money discount = eligibleTotal.multiplyByPercent(rule.getPercentOff());
        return AppliedDiscount.applied(rule, discount,
                rule.getPercentOff() + "% off on " + rule.getTargetBrand() + " items");
    }
}