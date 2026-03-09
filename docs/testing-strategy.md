# Testing Strategy

## What MUST Be Unit Tested

- **Discount calculation correctness per type**: Each of the 4 discount types (brand, category, voucher, payment) should have isolated tests verifying the percentage is applied to the correct base amount. This is the core business logic — if it's wrong, customers are overcharged or the company loses money.

- **Max cap enforcement**: Verify that when a calculated discount exceeds `maxCap`, the applied amount is capped. Edge case: discount exactly equals the cap.

- **Brand exclusion in vouchers**: Verify that items from excluded brands are not included in the voucher's eligible base. This is a common source of bugs because it requires filtering before calculation.

- **Application order consistency**: Verify that regardless of input order, discounts are always applied in BRAND → CATEGORY → VOUCHER → PAYMENT order. The final price must be deterministic.

## What Needs Integration Testing

- **Full cart scenario with all 4 discount types active**: Use the example from the requirements (2× PUMA T-shirts + 1× Nike Shoes with all 4 rules). Verify the final price, total discount, and that each rule's applied/skipped status is correct. This catches interaction bugs between discount types.

- **Stacking behavior**: A cart where both a brand discount and a stackable category discount apply to the same items. Verify the category discount is calculated on the already-reduced price (not the original).

- **Edge case: cart total reaches ₹0**: Apply aggressive discounts that would reduce the price below zero. Verify the engine floors at ₹0 and doesn't produce negative prices.

## What I Would NOT Test

- **Money arithmetic in isolation**: `BigDecimal` math is well-tested by the JDK. Testing that `999 * 0.4 = 399.60` is testing the language, not our logic. The Money class is a thin wrapper — its correctness is validated transitively through the engine tests.

- **Builder pattern / getters**: Testing that `rule.getTargetBrand()` returns what was passed to `.targetBrand()` is pure boilerplate. These are Java records and a straightforward builder — no logic to break.

- **Serialization / JSON mapping**: We don't have a persistence or API layer. Testing JSON serialization of domain objects would be testing code that doesn't exist yet. Write these tests when you build the API.

- **Exhaustive permutations of discount combinations**: With 4 types and various constraints, the combinatorial space is huge. Testing every permutation is wasteful. Focus on the documented scenarios and known edge cases. Property-based testing could help here if the team has the tooling.

## Pseudocode Test Cases

### Test 1: Voucher excludes Nike, caps at ₹500
```
given:
    cart = [2× PUMA T-shirt @ ₹999, 1× Nike Shoes @ ₹4999]
    voucher = SUPER69 (69% off, excludes Nike, maxCap ₹500)

when:
    result = engine.calculateDiscounts(cart, [voucher])

then:
    // Eligible base = 2 × 999 = ₹1998 (Nike excluded)
    // Raw discount = 1998 × 0.69 = ₹1378.62
    // Capped at ₹500
    assert result.appliedDiscounts[0].discountAmount == Money.of(500)
    assert result.finalPrice == Money.of(6497)  // 6997 - 500
```

### Test 2: Payment offer skipped when cart below minimum
```
given:
    cart = [1× Socks @ ₹199]
    paymentOffer = 10% ICICI discount, minCartValue ₹2000
    payment = ICICI credit card

when:
    result = engine.calculateDiscounts(cart, [paymentOffer])

then:
    assert result.appliedDiscounts[0].applied == false
    assert result.appliedDiscounts[0].reason.contains("below minimum")
    assert result.finalPrice == Money.of(199)
```
