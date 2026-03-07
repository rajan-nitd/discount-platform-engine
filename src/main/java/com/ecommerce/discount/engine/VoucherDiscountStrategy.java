package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

public class VoucherDiscountStrategy implements DiscountStrategy {

    @Override
    public AppliedDiscount apply(DiscountRule rule, Cart cart, Money runningTotal) {
        // BUG: applies voucher to full cart total, ignoring excludedBrands
        Money discount = runningTotal.multiplyByPercent(rule.getPercentOff());
        // BUG: maxCap is not enforced
        return AppliedDiscount.applied(rule, discount,
                "Voucher " + rule.getVoucherCode() + ": " + rule.getPercentOff() + "% off");
    }
}
