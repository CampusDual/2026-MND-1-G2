package com.campusdual.cd2026mnd1g1.api.dto;

public record StockProductDto(
        Long productId,
        String name,
        String description,
        String stockType,
        Integer uds,
        Integer warehouseUnits,
        Integer laundryUnits,
        Integer minimumStock,
        String status,
        Long subcategoryId,
        String subcategoryName,
        Boolean active
) {
}
