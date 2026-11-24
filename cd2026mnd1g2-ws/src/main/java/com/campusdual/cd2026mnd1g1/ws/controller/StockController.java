package com.campusdual.cd2026mnd1g1.ws.controller;

import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;
import com.campusdual.cd2026mnd1g1.api.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Devuelve el resumen de productos de stock disponibles para solicitudes de servicio.
     *
     * @return productos y numero de productos diferentes
     */
    @GetMapping
    public ResponseEntity<StockSummaryDto> getStock() {
        return ResponseEntity.ok(stockService.findStock());
    }
}
