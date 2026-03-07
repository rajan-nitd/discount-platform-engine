package com.ecommerce.discount.model;

import java.util.List;

/**
 * Represents a single item in the cart.
 */
public class CartItem {

    private String productName;
    private String brand;
    private String category;
    private Money unitPrice;
    private int quantity;

    public CartItem(String productName, String brand, String category, Money unitPrice, int quantity) {
        this.productName = productName;
        this.brand = brand;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public Money getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public Money lineTotal() {
        return Money.of(unitPrice.getAmount() * quantity);
    }
}
