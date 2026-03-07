# Discount Platform

Core discount calculation engine for a fashion e-commerce platform.

## Supported Discount Types
- Brand discounts (e.g., 40% off PUMA)
- Category discounts (e.g., 10% off T-shirts)
- Voucher codes (e.g., SUPER69 with constraints)
- Payment offers (e.g., 10% ICICI credit card discount)

## Running

```bash
javac -d out $(find src -name "*.java")
java -cp out com.ecommerce.discount.Main
```

## Structure
```
src/main/java/com/ecommerce/discount/
├── Main.java
├── engine/
│   ├── DiscountEngine.java
│   ├── DiscountStrategy.java
│   ├── BrandDiscountStrategy.java
│   ├── CategoryDiscountStrategy.java
│   ├── VoucherDiscountStrategy.java
│   └── PaymentDiscountStrategy.java
└── model/
    ├── AppliedDiscount.java
    ├── Cart.java
    ├── CartItem.java
    ├── DiscountResult.java
    ├── DiscountRule.java
    ├── DiscountType.java
    ├── Money.java
    └── PaymentContext.java
```
