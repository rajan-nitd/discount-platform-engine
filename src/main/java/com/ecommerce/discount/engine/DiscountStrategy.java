package com.ecommerce.discount.engine;

import com.ecommerce.discount.model.*;

/**
 * Strategy interface for discount evaluation.
 */
public interface DiscountStrategy {
    AppliedDiscount apply(DiscountRule rule, Cart cart, Money runningTotal);
}
