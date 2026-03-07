package com.ecommerce.discount;

import com.ecommerce.discount.engine.DiscountEngine;
import com.ecommerce.discount.model.*;

import java.util.List;
import java.util.Set;

/**
 * Demo runner using the exact example from the requirements doc.
 */
public class Main {

    public static void main(String[] args) {
        // --- Build the cart ---
        Cart cart = new Cart(
                List.of(
                        new CartItem("PUMA T-shirt", "PUMA", "T-shirts", Money.of(999), 2),
                        new CartItem("Nike Shoes", "Nike", "Footwear", Money.of(4999), 1)
                ),
                new PaymentContext("CREDIT_CARD", "ICICI")
        );

        // --- Define discount rules ---
        List<DiscountRule> rules = List.of(
                DiscountRule.builder("BRAND_PUMA_40", DiscountType.BRAND, 40)
                        .description("Min 40% off on all PUMA items")
                        .targetBrand("PUMA")
                        .build(),

                DiscountRule.builder("CAT_TSHIRT_10", DiscountType.CATEGORY, 10)
                        .description("Extra 10% off on all T-shirts")
                        .targetCategory("T-shirts")
                        .stackable(true)
                        .build(),

                DiscountRule.builder("VOUCHER_SUPER69", DiscountType.VOUCHER, 69)
                        .description("SUPER69: 69% off with constraints")
                        .voucherCode("SUPER69")
                        .excludedBrands(Set.of("Nike"))
                        .maxCap(Money.of(500))
                        .build(),

                DiscountRule.builder("PAY_ICICI_10", DiscountType.PAYMENT_OFFER, 10)
                        .description("10% instant discount on ICICI credit cards")
                        .requiredPaymentMethod("CREDIT_CARD")
                        .requiredIssuer("ICICI")
                        .maxCap(Money.of(200))
                        .minCartValue(Money.of(2000))
                        .build()
        );

        // --- Calculate ---
        DiscountEngine engine = new DiscountEngine();
        DiscountResult result = engine.calculateDiscounts(cart, rules);

        // --- Print results ---
        System.out.println("=== Discount Calculation Result ===");
        System.out.println("Original Price : " + result.originalPrice());
        System.out.println("Total Discount : " + result.totalDiscount());
        System.out.println("Final Price    : " + result.finalPrice());
        System.out.println();

        System.out.println("--- Applied/Skipped Discounts ---");
        for (AppliedDiscount ad : result.appliedDiscounts()) {
            String status = ad.applied() ? "✓ APPLIED" : "✗ SKIPPED";
            System.out.printf("  [%s] %s (%s) — %s%n", status, ad.ruleId(), ad.discountAmount(), ad.reason());
        }

        System.out.println();
        System.out.println("--- Reasoning ---");
        System.out.println(result.reasoning());
    }
}
