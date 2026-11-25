package com.campusdual.cd2026mnd1g1;

import com.campusdual.cd2026mnd1g1.api.dto.StockCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockProductDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockUpdateRequestDto;
import com.campusdual.cd2026mnd1g1.api.service.StockService;
import com.campusdual.cd2026mnd1g1.model.entity.CabinEntity;
import com.campusdual.cd2026mnd1g1.model.entity.RequestSubcategoryEntity;
import com.campusdual.cd2026mnd1g1.model.entity.ServiceRequestEntity;
import com.campusdual.cd2026mnd1g1.model.exception.RequestConflictException;
import com.campusdual.cd2026mnd1g1.model.exception.StockInsufficientException;
import com.campusdual.cd2026mnd1g1.model.repository.CabinRepository;
import com.campusdual.cd2026mnd1g1.model.repository.RequestSubcategoryRepository;
import com.campusdual.cd2026mnd1g1.model.repository.ServiceRequestRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockMovementRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockRepository;
import com.campusdual.cd2026mnd1g1.model.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StockInventoryIT {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockMovementRepository movementRepository;

    @Autowired
    private RequestSubcategoryRepository subcategoryRepository;

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Test
    void reusableInventoryMovesBetweenWarehouseLaundryAndDisposalWithHistory() {
        StockProductDto product = createProduct("REUSABLE", 100, 20);

        StockMovementCreateRequestDto toLaundry = movement(
                "TO_LAUNDRY", 15, null, null, null);
        StockMovementDto sent = stockService.createMovement(product.productId(), toLaundry, "admin");
        StockMovementDto repeated = stockService.createMovement(product.productId(), toLaundry, "admin");
        assertThat(repeated.movementId()).isEqualTo(sent.movementId());
        assertThat(stockRepository.findById(product.productId()).orElseThrow().getWarehouseUnits()).isEqualTo(85);
        assertThat(stockRepository.findById(product.productId()).orElseThrow().getLaundryUnits()).isEqualTo(15);
        assertThat(stockRepository.findById(product.productId()).orElseThrow().getUds()).isEqualTo(100);

        stockService.createMovement(product.productId(),
                movement("FROM_LAUNDRY", 5, null, null, null), "admin");
        stockService.createMovement(product.productId(),
                movement("DISPOSAL", 5, "WAREHOUSE", "Deteriorado", null), "admin");

        var stored = stockRepository.findById(product.productId()).orElseThrow();
        assertThat(stored.getWarehouseUnits()).isEqualTo(85);
        assertThat(stored.getLaundryUnits()).isEqualTo(10);
        assertThat(stored.getUds()).isEqualTo(95);
        assertThat(movementRepository.findAllByProductProductIdOrderByCreatedAtDescMovementIdDesc(
                product.productId())).hasSize(4);
    }

    @Test
    void rejectsOperationsThatWouldCreateNegativeInventory() {
        StockProductDto product = createProduct("REUSABLE", 4, 1);
        StockMovementCreateRequestDto request = movement("TO_LAUNDRY", 5, null, null, null);

        assertThatThrownBy(() -> stockService.createMovement(product.productId(), request, "admin"))
                .isInstanceOf(StockInsufficientException.class);
        assertThat(stockRepository.findById(product.productId()).orElseThrow().getWarehouseUnits()).isEqualTo(4);
        assertThat(movementRepository.findAllByProductProductIdOrderByCreatedAtDescMovementIdDesc(
                product.productId())).hasSize(1);
    }

    @Test
    void consumableEntriesAndMinimumStockChangesArePersistedWithoutChangingTypeOrCounts() {
        StockProductDto product = createProduct("CONSUMABLE", 100, 100);
        assertThat(product.status()).isEqualTo("REORDER");

        stockService.updateProduct(product.productId(),
                new StockUpdateRequestDto("Updated " + product.name(), "Updated description", 80));
        assertThat(stockService.findStock().products().stream()
                .filter(row -> row.productId().equals(product.productId()))
                .findFirst().orElseThrow().status()).isEqualTo("REORDER_SOON");
        stockService.updateProduct(product.productId(),
                new StockUpdateRequestDto("Updated " + product.name(), "Updated description", 79));
        assertThat(stockService.findStock().products().stream()
                .filter(row -> row.productId().equals(product.productId()))
                .findFirst().orElseThrow().status()).isEqualTo("CORRECT");
        StockMovementDto entry = stockService.createMovement(product.productId(),
                movement("STOCK_ENTRY", 5, null, null, null), "admin");

        var stored = stockRepository.findById(product.productId()).orElseThrow();
        assertThat(stored.getName()).startsWith("Updated ");
        assertThat(stored.getMinimumStock()).isEqualTo(79);
        assertThat(stored.getStockType()).isEqualTo("CONSUMABLE");
        assertThat(stored.getUds()).isEqualTo(105);
        assertThat(stored.getWarehouseUnits()).isEqualTo(105);
        assertThat(stored.getLaundryUnits()).isZero();
        assertThat(entry.previousTotal()).isEqualTo(100);
        assertThat(entry.nextTotal()).isEqualTo(105);
    }

    @Test
    void rejectsLaundryMovementsForConsumables() {
        StockProductDto product = createProduct("CONSUMABLE", 5, 1);

        assertThatThrownBy(() -> stockService.createMovement(
                product.productId(), movement("TO_LAUNDRY", 1, null, null, null), "admin"))
                .isInstanceOf(RequestConflictException.class);
        assertThat(stockRepository.findById(product.productId()).orElseThrow().getUds()).isEqualTo(5);
    }

    @Test
    void preventsCaseInsensitiveDuplicateProductNames() {
        StockProductDto product = createProduct("CONSUMABLE", 3, 1);
        String key = UUID.randomUUID().toString();

        assertThatThrownBy(() -> stockService.createProduct(new StockCreateRequestDto(
                product.name().toUpperCase(),
                "CONSUMABLE",
                0,
                0,
                null,
                null,
                key), "admin"))
                .isInstanceOf(RequestConflictException.class);
    }

    @Test
    void workerCanRecordMultipleReusableMovementsOnTheActiveHousekeepingTask() {
        RequestSubcategoryEntity subcategory = subcategoryRepository.findAll().stream()
                .filter(row -> "SERVICE".equals(row.getCategory().getType())
                        && row.getCategory().getName().startsWith("Toallas y"))
                .findFirst()
                .orElseThrow();
        CabinEntity cabin = cabinRepository.findAll().getFirst();
        var worker = workerRepository.findByUserUsername("admin").orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        ServiceRequestEntity request = new ServiceRequestEntity();
        request.setCabin(cabin);
        request.setCategory(subcategory.getCategory());
        request.setSubcategory(subcategory);
        request.setQuantity(2);
        request.setStatus("IN_PROGRESS");
        request.setWorker(worker);
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        ServiceRequestEntity persistedRequest = requestRepository.saveAndFlush(request);
        StockProductDto product = createProduct("REUSABLE", 30, 5);

        stockService.createMovement(product.productId(),
                movement("TO_LAUNDRY", 5, null, null, persistedRequest.getServiceRequestId()), "admin");
        stockService.createMovement(product.productId(),
                movement("DISPOSAL", 1, "LAUNDRY", "Perdido", persistedRequest.getServiceRequestId()), "admin");

        var stored = stockRepository.findById(product.productId()).orElseThrow();
        assertThat(stored.getWarehouseUnits()).isEqualTo(25);
        assertThat(stored.getLaundryUnits()).isEqualTo(4);
        assertThat(stored.getUds()).isEqualTo(29);
        var taskMovements = movementRepository.findAllByProductProductIdOrderByCreatedAtDescMovementIdDesc(
                        product.productId()).stream()
                .filter(movement -> persistedRequest.getServiceRequestId().equals(
                        movement.getServiceRequest() == null ? null
                                : movement.getServiceRequest().getServiceRequestId()))
                .toList();
        assertThat(taskMovements).hasSize(2);
        assertThat(taskMovements).allSatisfy(movement -> {
            assertThat(movement.getActorUsername()).isEqualTo("admin");
            assertThat(movement.getWorker().getWorkerId()).isEqualTo(worker.getWorkerId());
        });
    }

    private StockProductDto createProduct(String type, int initialUnits, int minimumStock) {
        String key = UUID.randomUUID().toString();
        return stockService.createProduct(new StockCreateRequestDto(
                "Integration product " + key,
                type,
                initialUnits,
                minimumStock,
                "Test product",
                null,
                key), "admin");
    }

    private StockMovementCreateRequestDto movement(
            String type,
            int quantity,
            String source,
            String reason,
            Long serviceRequestId) {
        return new StockMovementCreateRequestDto(
                type, quantity, source, reason, UUID.randomUUID().toString(), serviceRequestId);
    }
}
