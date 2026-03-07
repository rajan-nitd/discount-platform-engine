package com.ecommerce.discount.model;

import java.util.List;

/**
 * Represents a shopping cart with items and optional payment context.
 */
public class Cart {

    private List<CartItem> items;
    private PaymentContext paymentContext;

    public Cart(List<CartItem> items, PaymentContext paymentContext) {
        this.items = items;  // no defensive copy
        this.paymentContext = paymentContext;
    }

    public List<CartItem> getItems() { return items; }
    public PaymentContext getPaymentContext() { return paymentContext; }

    public Money totalBeforeDiscounts() {
        Money total = Money.ZERO;
        for (CartItem item : items) {
            total = total.add(item.lineTotal());
        }
        return total;
    }
}
