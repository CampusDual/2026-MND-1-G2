package com.campusdual.cd2026mnd1g1.api.dto;

import java.util.List;

public record StockSummaryDto(
        List<StockProductDto> products,
        Long differentProducts,
        Long correctProducts,
        Long reorderSoonProducts,
        Long reorderProducts
) {
}
