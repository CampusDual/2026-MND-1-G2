package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, Long> {

    List<StockMovementEntity> findAllByProductProductIdOrderByCreatedAtDescMovementIdDesc(Long productId);

    Optional<StockMovementEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<StockMovementEntity> findByServiceRequestServiceRequestIdAndMovementType(
            Long serviceRequestId,
            String movementType);
}
