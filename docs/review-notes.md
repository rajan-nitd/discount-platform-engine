# Review Notes — What I Fixed After the Initial AI-Generated Implementation

This document captures the changes made between the initial AI-generated commit and the reviewed version.

## 1. Money: Replaced `double` with `BigDecimal`

**Problem**: The initial implementation used `double` for monetary calculations. This is a classic precision bug — `0.1 + 0.2 != 0.3` in floating point. In a discount engine, rounding errors compound across multiple discounts and can lead to incorrect final prices.

**Fix**: Introduced a `Money` value object wrapping `BigDecimal` with `HALF_UP` rounding and 2 decimal places. All arithmetic goes through `Money` methods. This is the single most important fix — getting money wrong in e-commerce is a real production incident.

## 2. Removed Premature Abstraction

**Problem**: The initial version had a `DiscountStrategy` interface with separate classes for each discount type (`BrandDiscountStrategy`, `VoucherDiscountStrategy`, etc.). With only 4 types, this added indirection without value — you had to jump across 6 files to understand the flow.

**Fix**: Collapsed into a single `DiscountEngine` class with private methods per type and a `switch` expression. The logic is ~120 lines and reads top-to-bottom. If we grow to 8+ types, we can extract strategies then. YAGNI applies here.

## 3. Fixed Voucher Discount Base Calculation

**Problem**: The initial version applied the voucher percentage to the full cart total, ignoring `excludedBrands`. So `SUPER69` (69% off, excludes Nike) was incorrectly calculated on the Nike shoes too.

**Fix**: The voucher evaluation now iterates cart items and sums only those NOT in the excluded brands set. The 69% is applied to this eligible subtotal, not the full cart.

## 4. Added Max Cap Enforcement

**Problem**: The `maxCap` field existed on the rule but was never checked during calculation. A 69% voucher on a ₹10,000 cart would give ₹6,900 off instead of being capped at ₹500.

**Fix**: After calculating the raw discount amount, the engine checks `rule.getMaxCap()` and caps accordingly. Also added a safety check that discount never exceeds the remaining cart value.

## 5. Made Application Order Explicit and Documented

**Problem**: Rules were applied in insertion order, which is fragile and undocumented. Reordering the input list would silently change the final price.

**Fix**: Rules are sorted by `DiscountType.ordinal()` before processing. The enum defines the canonical order: `BRAND → CATEGORY → VOUCHER → PAYMENT_OFFER`. This is documented in the enum, the engine, and the README.

## 6. Added Reasoning/Audit Trail

**Problem**: The initial version returned only the final price. No visibility into which discounts were applied, skipped, or why.

**Fix**: Every rule evaluation produces an `AppliedDiscount` record with `applied/skipped` status and a human-readable reason. The `DiscountResult` includes a full reasoning string. This is critical for customer support ("why didn't my coupon work?") and debugging.

## 7. Defensive Copies on Collections

**Problem**: `Cart.items()` returned the mutable list passed to the constructor. External code could modify the cart's items after creation, leading to subtle bugs.

**Fix**: `Cart` record uses `List.copyOf(items)` in the compact constructor. `DiscountRule.excludedBrands` uses `Set.copyOf()`. All domain objects are now truly immutable.

## What I Chose NOT to Fix

- **No generics/abstraction for currency**: We only deal with INR. Adding multi-currency support now would be over-engineering.
- **No persistence layer**: The assignment explicitly says in-memory is fine.
- **No concurrent access handling**: The engine is stateless and takes immutable inputs, so it's inherently thread-safe. No need for synchronization.
