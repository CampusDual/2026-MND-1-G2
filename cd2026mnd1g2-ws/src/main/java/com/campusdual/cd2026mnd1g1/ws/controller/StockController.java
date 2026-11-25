package com.campusdual.cd2026mnd1g1.ws.controller;

import com.campusdual.cd2026mnd1g1.api.dto.StockCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockProductDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockUpdateRequestDto;
import com.campusdual.cd2026mnd1g1.api.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

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

    /**
     * Crea un producto y registra su stock inicial.
     *
     * @param request datos del producto
     * @param principal usuario autenticado
     * @return producto creado
     */
    @PostMapping
    public ResponseEntity<StockProductDto> createProduct(
            @RequestBody StockCreateRequestDto request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.createProduct(request, principal.getName()));
    }

    /**
     * Actualiza datos descriptivos y el mínimo de un producto existente.
     *
     * @param productId identificador del producto
     * @param request datos modificables
     * @return producto actualizado
     */
    @PutMapping("/{productId}")
    public ResponseEntity<StockProductDto> updateProduct(
            @PathVariable Long productId,
            @RequestBody StockUpdateRequestDto request) {
        return ResponseEntity.ok(stockService.updateProduct(productId, request));
    }

    /**
     * Registra un movimiento de inventario asociado opcionalmente a una tarea.
     *
     * @param productId identificador del producto
     * @param request datos del movimiento
     * @param principal usuario autenticado
     * @return movimiento persistido
     */
    @PostMapping("/{productId}/movements")
    public ResponseEntity<StockMovementDto> createMovement(
            @PathVariable Long productId,
            @RequestBody StockMovementCreateRequestDto request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.createMovement(productId, request, principal.getName()));
    }

    /**
     * Consulta el historial de movimientos de un producto.
     *
     * @param productId identificador del producto
     * @return historial ordenado del más reciente al más antiguo
     */
    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<StockMovementDto>> findMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findMovements(productId));
    }
}
