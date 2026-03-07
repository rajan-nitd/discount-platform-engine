package com.ecommerce.discount.model;

import java.util.List;

/**
 * Represents a shopping cart with items and optional payment context.
 */
public record Cart(
        List<CartItem> items,
        PaymentContext paymentContext
) {
    public Cart {
        items = List.copyOf(items); // defensive copy, immutable
    }

    public Money totalBeforeDiscounts() {
        Money total = Money.ZERO;
        for (CartItem item : items) {
            total = total.add(item.lineTotal());
        }
        return total;
    }
}
