package com.campusdual.cd2026mnd1g1.model.service;

import com.campusdual.cd2026mnd1g1.api.dto.StockProductDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;
import com.campusdual.cd2026mnd1g1.api.service.StockService;
import com.campusdual.cd2026mnd1g1.model.entity.RequestSubcategoryEntity;
import com.campusdual.cd2026mnd1g1.model.entity.StockEntity;
import com.campusdual.cd2026mnd1g1.model.repository.RequestSubcategoryRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final RequestSubcategoryRepository subcategoryRepository;

    public StockServiceImpl(StockRepository stockRepository,
                            RequestSubcategoryRepository subcategoryRepository) {
        this.stockRepository = stockRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StockSummaryDto findStock() {
        List<StockProductDto> products = stockRepository.findServiceProducts().stream()
                .map(this::mapProduct)
                .toList();
        return new StockSummaryDto(products, stockRepository.countServiceProducts());
    }

    @Override
    public void validateSubcategory(Long subcategoryId) {
        if (subcategoryId == null || subcategoryId <= 0) {
            throw new IllegalArgumentException("La subcategoria no es valida");
        }
        RequestSubcategoryEntity subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new IllegalArgumentException("La subcategoria no existe"));
        if (!"SERVICE".equals(subcategory.getCategory().getType())) {
            throw new IllegalArgumentException(
                    "Solo se pueden crear productos de stock para subcategorias de tipo SERVICE");
        }
    }

    private StockProductDto mapProduct(StockEntity stock) {
        return new StockProductDto(stock.getName(), stock.getUds());
    }
}
