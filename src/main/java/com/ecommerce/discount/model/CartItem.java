package com.ecommerce.discount.model;

/**
 * Represents a single item in the cart.
 */
public record CartItem(
        String productName,
        String brand,
        String category,
        Money unitPrice,
        int quantity
) {
    public CartItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public Money lineTotal() {
        Money total = Money.ZERO;
        for (int i = 0; i < quantity; i++) {
            total = total.add(unitPrice);
        }
        return total;
    }
}
