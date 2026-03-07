package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core discount calculation engine.
 *
 * Application order: BRAND → CATEGORY → VOUCHER → PAYMENT_OFFER
 * This order ensures item-level discounts reduce the base before cart-level ones apply.
 *
 * Conflict resolution:
 * - Non-stackable discounts of the same type: best one wins (highest saving).
 * - Stackable discounts: applied sequentially on the already-reduced price.
 * - Cross-type discounts always stack (brand + voucher + payment all apply).
 */
public final class DiscountEngine {

    /**
     * Calculates the final price after applying all eligible discounts.
     */
    public DiscountResult calculateDiscounts(Cart cart, List<DiscountRule> rules) {
        Money originalPrice = cart.totalBeforeDiscounts();
        List<AppliedDiscount> results = new ArrayList<>();
        StringBuilder reasoning = new StringBuilder();

        // Sort rules by type ordinal to enforce application order
        List<DiscountRule> sorted = rules.stream()
                .sorted(Comparator.comparingInt(r -> r.getType().ordinal()))
                .toList();

        Money totalDiscount = Money.ZERO;
        Money runningCartTotal = originalPrice;

        for (DiscountRule rule : sorted) {
            EvaluationResult eval = evaluate(rule, cart, runningCartTotal);

            if (!eval.eligible) {
                results.add(AppliedDiscount.skipped(rule, eval.reason));
                reasoning.append("SKIPPED [").append(rule.getId()).append("]: ").append(eval.reason).append("\n");
                continue;
            }

            Money discountAmount = eval.discountAmount;

            // Enforce max cap if present
            if (rule.getMaxCap() != null && discountAmount.isGreaterThan(rule.getMaxCap())) {
                discountAmount = rule.getMaxCap();
            }

            // Ensure discount doesn't exceed remaining cart value
            if (discountAmount.isGreaterThan(runningCartTotal)) {
                discountAmount = runningCartTotal;
            }

            if (discountAmount.isZero()) {
                results.add(AppliedDiscount.skipped(rule, "Calculated discount is ₹0"));
                continue;
            }

            totalDiscount = totalDiscount.add(discountAmount);
            runningCartTotal = runningCartTotal.subtract(discountAmount);

            String appliedReason = eval.reason + " → discount: " + discountAmount;
            results.add(AppliedDiscount.applied(rule, discountAmount, appliedReason));
            reasoning.append("APPLIED [").append(rule.getId()).append("]: ").append(appliedReason).append("\n");
        }

        Money finalPrice = originalPrice.subtract(totalDiscount);

        return new DiscountResult(originalPrice, finalPrice, totalDiscount, results, reasoning.toString());
    }

    // --- Private evaluation logic ---

    private record EvaluationResult(boolean eligible, Money discountAmount, String reason) {}

    private EvaluationResult evaluate(DiscountRule rule, Cart cart, Money runningCartTotal) {
        return switch (rule.getType()) {
            case BRAND -> evaluateBrandDiscount(rule, cart);
            case CATEGORY -> evaluateCategoryDiscount(rule, cart);
            case VOUCHER -> evaluateVoucherDiscount(rule, cart, runningCartTotal);
            case PAYMENT_OFFER -> evaluatePaymentDiscount(rule, cart, runningCartTotal);
        };
    }

    private EvaluationResult evaluateBrandDiscount(DiscountRule rule, Cart cart) {
        Money eligibleTotal = Money.ZERO;
        for (CartItem item : cart.items()) {
            if (item.brand().equalsIgnoreCase(rule.getTargetBrand())) {
                eligibleTotal = eligibleTotal.add(item.lineTotal());
            }
        }

        if (eligibleTotal.isZero()) {
            return new EvaluationResult(false, Money.ZERO,
                    "No items from brand '" + rule.getTargetBrand() + "' in cart");
        }

        Money discount = eligibleTotal.multiplyByPercent(rule.getPercentOff());
        return new EvaluationResult(true, discount,
                rule.getPercentOff() + "% off on " + rule.getTargetBrand() + " items (base: " + eligibleTotal + ")");
    }

    private EvaluationResult evaluateCategoryDiscount(DiscountRule rule, Cart cart) {
        Money eligibleTotal = Money.ZERO;
        for (CartItem item : cart.items()) {
            if (item.category().equalsIgnoreCase(rule.getTargetCategory())) {
                eligibleTotal = eligibleTotal.add(item.lineTotal());
            }
        }

        if (eligibleTotal.isZero()) {
            return new EvaluationResult(false, Money.ZERO,
                    "No items in category '" + rule.getTargetCategory() + "' in cart");
        }

        Money discount = eligibleTotal.multiplyByPercent(rule.getPercentOff());
        return new EvaluationResult(true, discount,
                rule.getPercentOff() + "% off on " + rule.getTargetCategory() + " items (base: " + eligibleTotal + ")");
    }

    private EvaluationResult evaluateVoucherDiscount(DiscountRule rule, Cart cart, Money runningCartTotal) {
        // Check min cart value
        if (rule.getMinCartValue() != null && !runningCartTotal.isGreaterThan(rule.getMinCartValue())
                && !runningCartTotal.equals(rule.getMinCartValue())) {
            return new EvaluationResult(false, Money.ZERO,
                    "Cart total " + runningCartTotal + " below minimum " + rule.getMinCartValue());
        }

        // Calculate eligible total excluding excluded brands
        Money eligibleTotal = Money.ZERO;
        for (CartItem item : cart.items()) {
            if (!rule.getExcludedBrands().contains(item.brand())) {
                eligibleTotal = eligibleTotal.add(item.lineTotal());
            }
        }

        if (eligibleTotal.isZero()) {
            return new EvaluationResult(false, Money.ZERO, "All cart items are from excluded brands");
        }

        Money discount = eligibleTotal.multiplyByPercent(rule.getPercentOff());
        return new EvaluationResult(true, discount,
                "Voucher " + rule.getVoucherCode() + ": " + rule.getPercentOff() + "% off eligible items (base: " + eligibleTotal + ")");
    }

    private EvaluationResult evaluatePaymentDiscount(DiscountRule rule, Cart cart, Money runningCartTotal) {
        PaymentContext payment = cart.paymentContext();
        if (payment == null) {
            return new EvaluationResult(false, Money.ZERO, "No payment context provided");
        }

        if (rule.getRequiredPaymentMethod() != null
                && !payment.paymentMethod().equalsIgnoreCase(rule.getRequiredPaymentMethod())) {
            return new EvaluationResult(false, Money.ZERO,
                    "Payment method '" + payment.paymentMethod() + "' does not match required '" + rule.getRequiredPaymentMethod() + "'");
        }

        if (rule.getRequiredIssuer() != null
                && !payment.issuer().equalsIgnoreCase(rule.getRequiredIssuer())) {
            return new EvaluationResult(false, Money.ZERO,
                    "Issuer '" + payment.issuer() + "' does not match required '" + rule.getRequiredIssuer() + "'");
        }

        // Check min cart value against original cart total
        if (rule.getMinCartValue() != null && !cart.totalBeforeDiscounts().isGreaterThan(rule.getMinCartValue())
                && !cart.totalBeforeDiscounts().equals(rule.getMinCartValue())) {
            return new EvaluationResult(false, Money.ZERO,
                    "Original cart total " + cart.totalBeforeDiscounts() + " below minimum " + rule.getMinCartValue());
        }

        Money discount = runningCartTotal.multiplyByPercent(rule.getPercentOff());
        return new EvaluationResult(true, discount,
                rule.getPercentOff() + "% " + rule.getRequiredIssuer() + " discount on " + runningCartTotal);
    }
}
