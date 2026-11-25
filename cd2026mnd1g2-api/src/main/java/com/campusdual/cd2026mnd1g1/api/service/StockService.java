package com.campusdual.cd2026mnd1g1.api.service;

import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockUpdateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockProductDto;

import java.util.List;

public interface StockService {

    StockSummaryDto findStock();

    StockProductDto createProduct(StockCreateRequestDto request, String actorUsername);

    StockProductDto updateProduct(Long productId, StockUpdateRequestDto request);

    List<StockMovementDto> findMovements(Long productId);

    StockMovementDto createMovement(
            Long productId,
            StockMovementCreateRequestDto request,
            String actorUsername);

    void recordRequestConsumption(
            Long subcategoryId,
            Integer quantity,
            Long serviceRequestId,
            String actorUsername);

    void validateSubcategory(Long subcategoryId);
}
