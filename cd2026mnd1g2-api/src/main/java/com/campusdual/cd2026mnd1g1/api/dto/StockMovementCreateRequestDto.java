package com.campusdual.cd2026mnd1g1.api.dto;

public record StockMovementCreateRequestDto(
        String movementType,
        Integer quantity,
        String sourceState,
        String reason,
        String idempotencyKey,
        Long serviceRequestId
) {
}
