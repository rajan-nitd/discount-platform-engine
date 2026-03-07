package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core discount calculation engine.
 * Initial AI-generated version using Strategy pattern.
 *
 * Known issues (to be fixed in review):
 * - Rules applied in insertion order, not by type priority
 * - Voucher ignores excludedBrands
 * - maxCap not enforced
 * - No minCartValue check on payment offers
 * - Uses double for money (precision issues)
 * - No defensive copies on collections
 */
public class DiscountEngine {

    private final Map<DiscountType, DiscountStrategy> strategies;

    public DiscountEngine() {
        strategies = new HashMap<>();
        strategies.put(DiscountType.BRAND, new BrandDiscountStrategy());
        strategies.put(DiscountType.CATEGORY, new CategoryDiscountStrategy());
        strategies.put(DiscountType.VOUCHER, new VoucherDiscountStrategy());
        strategies.put(DiscountType.PAYMENT_OFFER, new PaymentDiscountStrategy());
    }

    public DiscountResult calculateDiscounts(Cart cart, List<DiscountRule> rules) {
        Money originalPrice = cart.totalBeforeDiscounts();
        List<AppliedDiscount> results = new ArrayList<>();
        Money totalDiscount = Money.ZERO;
        Money runningTotal = originalPrice;

        // BUG: rules applied in insertion order, not sorted by type
        for (DiscountRule rule : rules) {
            DiscountStrategy strategy = strategies.get(rule.getType());
            if (strategy == null) {
                results.add(AppliedDiscount.skipped(rule, "No strategy for type: " + rule.getType()));
                continue;
            }

            AppliedDiscount result = strategy.apply(rule, cart, runningTotal);
            results.add(result);

            if (result.applied()) {
                totalDiscount = totalDiscount.add(result.discountAmount());
                runningTotal = runningTotal.subtract(result.discountAmount());
            }
        }

        Money finalPrice = originalPrice.subtract(totalDiscount);
        return new DiscountResult(originalPrice, finalPrice, totalDiscount, results, "");
    }
}
