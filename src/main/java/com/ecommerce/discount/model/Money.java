package com.ecommerce.discount.model;

/**
 * Represents a monetary amount.
 * Initial implementation using double for simplicity.
 */
public class Money {

    public static final Money ZERO = new Money(0);

    private final double amount;

    public Money(double amount) {
        this.amount = amount;
    }

    public static Money of(double amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        return new Money(this.amount - other.amount);
    }

    public Money multiplyByPercent(double percent) {
        return new Money(this.amount * percent / 100.0);
    }

    public Money min(Money other) {
        return this.amount <= other.amount ? this : other;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    public boolean isZero() {
        return this.amount == 0;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "₹" + String.format("%.2f", amount);
    }
}
