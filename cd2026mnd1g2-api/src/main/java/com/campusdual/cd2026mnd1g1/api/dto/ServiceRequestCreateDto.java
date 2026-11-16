package com.campusdual.cd2026mnd1g1.api.dto;

public record ServiceRequestCreateDto(Long categoryId, Long subcategoryId, Integer quantity,
                                      String preferredDelivery) {
}
