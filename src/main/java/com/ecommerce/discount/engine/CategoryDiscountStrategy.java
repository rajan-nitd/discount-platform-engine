package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

public class CategoryDiscountStrategy implements DiscountStrategy {

    @Override
    public AppliedDiscount apply(DiscountRule rule, Cart cart, Money runningTotal) {
        Money eligibleTotal = Money.ZERO;
        for (CartItem item : cart.getItems()) {
            if (item.getCategory().equalsIgnoreCase(rule.getTargetCategory())) {
                eligibleTotal = eligibleTotal.add(item.lineTotal());
            }
        }

        if (eligibleTotal.isZero()) {
            return AppliedDiscount.skipped(rule, "No items in category '" + rule.getTargetCategory() + "'");
        }

        Money discount = eligibleTotal.multiplyByPercent(rule.getPercentOff());
        return AppliedDiscount.applied(rule, discount,
                rule.getPercentOff() + "% off on " + rule.getTargetCategory() + " items");
    }
}
