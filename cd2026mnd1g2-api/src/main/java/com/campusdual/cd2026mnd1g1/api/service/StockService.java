package com.campusdual.cd2026mnd1g1.api.service;

import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;

public interface StockService {

    StockSummaryDto findStock();

    void validateSubcategory(Long subcategoryId);
}
