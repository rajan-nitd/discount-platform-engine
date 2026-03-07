package com.ecommerce.discount.model;

import java.util.Set;

/**
 * Defines a discount rule with eligibility criteria and constraints.
 *
 * Design choices:
 * - Single class with optional fields rather than a deep hierarchy.
 *   For 4 discount types this is simpler and more readable than a polymorphic tree.
 * - Constraints (maxCap, minCartValue, excludedBrands) are optional and null-safe.
 */
public final class DiscountRule {

    private final String id;
    private final String description;
    private final DiscountType type;
    private final double percentOff;
    private final boolean stackable;

    // Targeting criteria (nullable — null means "applies to all")
    private final String targetBrand;
    private final String targetCategory;
    private final String voucherCode;
    private final String requiredPaymentMethod;
    private final String requiredIssuer;

    // Constraints
    private final Money maxCap;
    private final Money minCartValue;
    private final Set<String> excludedBrands;

    private DiscountRule(Builder b) {
        this.id = b.id;
        this.description = b.description;
        this.type = b.type;
        this.percentOff = b.percentOff;
        this.stackable = b.stackable;
        this.targetBrand = b.targetBrand;
        this.targetCategory = b.targetCategory;
        this.voucherCode = b.voucherCode;
        this.requiredPaymentMethod = b.requiredPaymentMethod;
        this.requiredIssuer = b.requiredIssuer;
        this.maxCap = b.maxCap;
        this.minCartValue = b.minCartValue;
        this.excludedBrands = b.excludedBrands != null ? Set.copyOf(b.excludedBrands) : Set.of();
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getDescription() { return description; }
    public DiscountType getType() { return type; }
    public double getPercentOff() { return percentOff; }
    public boolean isStackable() { return stackable; }
    public String getTargetBrand() { return targetBrand; }
    public String getTargetCategory() { return targetCategory; }
    public String getVoucherCode() { return voucherCode; }
    public String getRequiredPaymentMethod() { return requiredPaymentMethod; }
    public String getRequiredIssuer() { return requiredIssuer; }
    public Money getMaxCap() { return maxCap; }
    public Money getMinCartValue() { return minCartValue; }
    public Set<String> getExcludedBrands() { return excludedBrands; }

    // --- Builder ---

    public static Builder builder(String id, DiscountType type, double percentOff) {
        return new Builder(id, type, percentOff);
    }

    public static final class Builder {
        private final String id;
        private final DiscountType type;
        private final double percentOff;
        private String description = "";
        private boolean stackable = false;
        private String targetBrand;
        private String targetCategory;
        private String voucherCode;
        private String requiredPaymentMethod;
        private String requiredIssuer;
        private Money maxCap;
        private Money minCartValue;
        private Set<String> excludedBrands;

        private Builder(String id, DiscountType type, double percentOff) {
            this.id = id;
            this.type = type;
            this.percentOff = percentOff;
        }

        public Builder description(String d) { this.description = d; return this; }
        public Builder stackable(boolean s) { this.stackable = s; return this; }
        public Builder targetBrand(String b) { this.targetBrand = b; return this; }
        public Builder targetCategory(String c) { this.targetCategory = c; return this; }
        public Builder voucherCode(String v) { this.voucherCode = v; return this; }
        public Builder requiredPaymentMethod(String m) { this.requiredPaymentMethod = m; return this; }
        public Builder requiredIssuer(String i) { this.requiredIssuer = i; return this; }
        public Builder maxCap(Money m) { this.maxCap = m; return this; }
        public Builder minCartValue(Money m) { this.minCartValue = m; return this; }
        public Builder excludedBrands(Set<String> b) { this.excludedBrands = b; return this; }

        public DiscountRule build() {
            return new DiscountRule(this);
        }
    }
}
