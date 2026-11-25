package com.campusdual.cd2026mnd1g1.api.dto;

public record StockCreateRequestDto(
        String name,
        String stockType,
        Integer initialUnits,
        Integer minimumStock,
        String description,
        Long subcategoryId,
        String idempotencyKey
) {
}
