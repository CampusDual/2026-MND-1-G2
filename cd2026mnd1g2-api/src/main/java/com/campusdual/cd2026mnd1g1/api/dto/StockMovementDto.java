package com.campusdual.cd2026mnd1g1.api.dto;

import java.time.LocalDateTime;

public record StockMovementDto(
        Long movementId,
        Long productId,
        String productName,
        String movementType,
        Integer quantity,
        String sourceState,
        String reason,
        LocalDateTime createdAt,
        String actorUsername,
        Long workerId,
        Long serviceRequestId,
        Integer previousWarehouse,
        Integer previousLaundry,
        Integer previousTotal,
        Integer nextWarehouse,
        Integer nextLaundry,
        Integer nextTotal
) {
}
