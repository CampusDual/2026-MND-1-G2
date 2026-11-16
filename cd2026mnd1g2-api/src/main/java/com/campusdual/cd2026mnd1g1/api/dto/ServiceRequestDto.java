package com.campusdual.cd2026mnd1g1.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ServiceRequestDto(Long serviceRequestId, Integer cabinNumber, RequestCategoryDto category,
                                RequestSubcategoryDto subcategory, Integer quantity, String preferredDelivery,
                                String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                List<RequestStatusHistoryDto> statusHistory) {
}
