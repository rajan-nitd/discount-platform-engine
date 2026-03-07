package com.ecommerce.discount.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a monetary amount in INR.
 * Uses BigDecimal to avoid floating-point precision issues.
 */
public final class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money of(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        return new Money(result);
    }

    public Money multiplyByPercent(double percent) {
        BigDecimal factor = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return new Money(this.amount.multiply(factor));
    }

    public Money min(Money other) {
        return this.amount.compareTo(other.amount) <= 0 ? this : other;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "₹" + amount.toPlainString();
    }
}
