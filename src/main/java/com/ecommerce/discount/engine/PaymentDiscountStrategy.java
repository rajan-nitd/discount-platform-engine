package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

public class PaymentDiscountStrategy implements DiscountStrategy {

    @Override
    public AppliedDiscount apply(DiscountRule rule, Cart cart, Money runningTotal) {
        PaymentContext payment = cart.getPaymentContext();
        if (payment == null) {
            return AppliedDiscount.skipped(rule, "No payment context provided");
        }

        if (rule.getRequiredPaymentMethod() != null
                && !payment.getPaymentMethod().equalsIgnoreCase(rule.getRequiredPaymentMethod())) {
            return AppliedDiscount.skipped(rule, "Payment method mismatch");
        }

        if (rule.getRequiredIssuer() != null
                && !payment.getIssuer().equalsIgnoreCase(rule.getRequiredIssuer())) {
            return AppliedDiscount.skipped(rule, "Issuer mismatch");
        }

        // BUG: no minCartValue check
        Money discount = runningTotal.multiplyByPercent(rule.getPercentOff());
        // BUG: maxCap not enforced
        return AppliedDiscount.applied(rule, discount,
                rule.getPercentOff() + "% " + rule.getRequiredIssuer() + " discount");
    }
}
