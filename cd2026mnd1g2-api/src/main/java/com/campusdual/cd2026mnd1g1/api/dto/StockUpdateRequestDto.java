package com.campusdual.cd2026mnd1g1.api.dto;

public record StockUpdateRequestDto(
        String name,
        String description,
        Integer minimumStock
) {
}
