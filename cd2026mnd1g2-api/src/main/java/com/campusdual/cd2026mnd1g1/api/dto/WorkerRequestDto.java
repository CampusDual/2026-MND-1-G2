package com.campusdual.cd2026mnd1g1.api.dto;

public record WorkerRequestDto(
        Long serviceRequestId,
        Integer cabinNumber,
        String itemName,
        Integer quantity,
        String status
) {
}
