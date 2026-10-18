package com.syos.domain.repository;

import com.syos.domain.entity.LoyaltyTier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface LoyaltyTierRepository {


    List<LoyaltyTier> findAllOrderedByMinPurchasesDesc();


    Optional<LoyaltyTier> findTierByPurchaseAmount(BigDecimal totalPurchases);


    Optional<LoyaltyTier> findByName(String tierName);
}