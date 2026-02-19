package com.moonlight.project.airBnbApp.strategy;

import com.moonlight.project.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {



    BigDecimal calculatePrice(Inventory inventory);
}
