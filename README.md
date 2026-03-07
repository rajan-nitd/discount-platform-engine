# Discount Platform

A core discount calculation engine for a fashion e-commerce platform. Supports brand discounts, category discounts, voucher codes, and payment offers — with configurable stacking, ordering, and cap constraints.

## Language Choice: Java 17

Java was chosen for:
- Strong type system — ideal for domain modeling (Money, Cart, DiscountRule)
- Records for immutable value objects (Java 17+)
- Sealed/switch expressions for exhaustive discount type handling
- Widely used in e-commerce backends at scale

## Canonical Models

### Cart & CartItem
```java
record CartItem(String productName, String brand, String category, Money unitPrice, int quantity)
record Cart(List<CartItem> items, PaymentContext paymentContext)
```

### Money (Value Object)
```java
// Wraps BigDecimal, prevents negative values, handles rounding
Money.of(999)          // ₹999.00
money.multiplyByPercent(40)  // 40% of the amount
money.subtract(other)        // floors at ₹0.00
```

### DiscountRule
```java
DiscountRule.builder("BRAND_PUMA_40", DiscountType.BRAND, 40)
    .description("Min 40% off on all PUMA items")
    .targetBrand("PUMA")
    .build();
```


## Example Rule Definitions

### 1. Brand Discount (JSON representation)
```json
{
  "id": "BRAND_PUMA_40",
  "type": "BRAND",
  "percentOff": 40,
  "targetBrand": "PUMA",
  "description": "Min 40% off on all PUMA items"
}
```

### 2. Voucher Code with Constraints
```json
{
  "id": "VOUCHER_SUPER69",
  "type": "VOUCHER",
  "percentOff": 69,
  "voucherCode": "SUPER69",
  "excludedBrands": ["Nike"],
  "maxCap": 500,
  "description": "69% off, excludes Nike, max ₹500"
}
```

### 3. Payment Offer
```json
{
  "id": "PAY_ICICI_10",
  "type": "PAYMENT_OFFER",
  "percentOff": 10,
  "requiredPaymentMethod": "CREDIT_CARD",
  "requiredIssuer": "ICICI",
  "maxCap": 200,
  "minCartValue": 2000,
  "description": "10% instant discount on ICICI credit cards"
}
```

## Constraints & Conflict Resolution

### Discount Application Order
Discounts are applied in a fixed priority order:
1. **BRAND** — item-level, reduces the base price of matching items
2. **CATEGORY** — item-level, applied on top of brand discounts (if stackable)
3. **VOUCHER** — cart-level, applied on the reduced cart total
4. **PAYMENT_OFFER** — applied last, on the final amount before payment

This order ensures item-level discounts reduce the base before cart-level ones compound.

### Conflicting Discounts
- **Same type, non-stackable**: Only the best discount (highest saving) is applied. Others are skipped with a reason.
- **Same type, stackable**: Applied sequentially on the already-reduced price (e.g., brand 40% then category 10% on T-shirts).
- **Cross-type**: Always stack. A brand discount + voucher + payment offer all apply in order.

### Upper Threshold Enforcement
- Each rule can define a `maxCap` (e.g., max ₹500 off). The engine caps the calculated discount at this value.
- The engine also ensures no discount exceeds the remaining cart value (price never goes below ₹0).

### Brand Exclusions
- Voucher rules can specify `excludedBrands`. Items from those brands are excluded from the voucher's eligible base.

### Minimum Cart Value
- Rules can require a `minCartValue`. If the cart total is below this threshold, the discount is skipped.

## Architecture

See `docs/architecture.md` for the full architecture diagram and data flow.

## Running the Demo

```bash
# Compile
javac -d out $(find src -name "*.java")

# Run
java -cp out com.ecommerce.discount.Main
```

## Project Structure
```
discount-platform/
├── README.md
├── pom.xml
├── docs/
│   ├── architecture.md
│   ├── review-notes.md
│   └── testing-strategy.md
└── src/main/java/com/ecommerce/discount/
    ├── Main.java                    # Demo runner
    ├── engine/
    │   └── DiscountEngine.java      # Core calculation logic
    └── model/
        ├── AppliedDiscount.java     # Result of applying/skipping a rule
        ├── Cart.java                # Shopping cart
        ├── CartItem.java            # Single cart item
        ├── DiscountResult.java      # Final calculation output
        ├── DiscountRule.java        # Rule definition with builder
        ├── DiscountType.java        # BRAND, CATEGORY, VOUCHER, PAYMENT_OFFER
        ├── Money.java               # Monetary value object
        └── PaymentContext.java      # Payment method info
```
