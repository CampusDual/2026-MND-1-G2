package com.campusdual.cd2026mnd1g1.model.service;

import com.campusdual.cd2026mnd1g1.api.dto.StockCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementCreateRequestDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockMovementDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockProductDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockSummaryDto;
import com.campusdual.cd2026mnd1g1.api.dto.StockUpdateRequestDto;
import com.campusdual.cd2026mnd1g1.api.service.StockService;
import com.campusdual.cd2026mnd1g1.model.entity.RequestSubcategoryEntity;
import com.campusdual.cd2026mnd1g1.model.entity.ServiceRequestEntity;
import com.campusdual.cd2026mnd1g1.model.entity.StockEntity;
import com.campusdual.cd2026mnd1g1.model.entity.StockMovementEntity;
import com.campusdual.cd2026mnd1g1.model.entity.WorkerEntity;
import com.campusdual.cd2026mnd1g1.model.exception.RequestConflictException;
import com.campusdual.cd2026mnd1g1.model.exception.ServiceRequestNotFoundException;
import com.campusdual.cd2026mnd1g1.model.exception.StockInsufficientException;
import com.campusdual.cd2026mnd1g1.model.exception.StockProductNotFoundException;
import com.campusdual.cd2026mnd1g1.model.exception.WorkerNotFoundException;
import com.campusdual.cd2026mnd1g1.model.repository.RequestSubcategoryRepository;
import com.campusdual.cd2026mnd1g1.model.repository.ServiceRequestRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockMovementRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockRepository;
import com.campusdual.cd2026mnd1g1.model.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class StockServiceImpl implements StockService {
    private static final String CONSUMABLE = "CONSUMABLE";
    private static final String REUSABLE = "REUSABLE";
    private static final String STOCK_ENTRY = "STOCK_ENTRY";
    private static final String CONSUMPTION = "CONSUMPTION";
    private static final String TO_LAUNDRY = "TO_LAUNDRY";
    private static final String FROM_LAUNDRY = "FROM_LAUNDRY";
    private static final String DISPOSAL = "DISPOSAL";
    private static final String WAREHOUSE = "WAREHOUSE";
    private static final String LAUNDRY = "LAUNDRY";
    private static final String CORRECT = "CORRECT";
    private static final String REORDER_SOON = "REORDER_SOON";
    private static final String REORDER = "REORDER";

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final RequestSubcategoryRepository subcategoryRepository;
    private final WorkerRepository workerRepository;
    private final ServiceRequestRepository requestRepository;

    public StockServiceImpl(
            StockRepository stockRepository,
            StockMovementRepository movementRepository,
            RequestSubcategoryRepository subcategoryRepository,
            WorkerRepository workerRepository,
            ServiceRequestRepository requestRepository) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.workerRepository = workerRepository;
        this.requestRepository = requestRepository;
    }

    /** Devuelve el inventario activo y sus contadores calculados desde las cantidades actuales. */
    @Override
    @Transactional(readOnly = true)
    public StockSummaryDto findStock() {
        List<StockProductDto> products = stockRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(this::mapProduct)
                .toList();
        long correct = products.stream().filter(product -> CORRECT.equals(product.status())).count();
        long reorderSoon = products.stream().filter(product -> REORDER_SOON.equals(product.status())).count();
        long reorder = products.stream().filter(product -> REORDER.equals(product.status())).count();
        return new StockSummaryDto(products, (long) products.size(), correct, reorderSoon, reorder);
    }

    /** Crea un producto y registra su inventario inicial como un movimiento de entrada. */
    @Override
    public StockProductDto createProduct(StockCreateRequestDto request, String actorUsername) {
        validateActor(actorUsername);
        if (request == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios");
        }
        String name = requiredText(request.name(), "El nombre del producto es obligatorio");
        String type = normalizeProductType(request.stockType());
        int initialUnits = requireNonNegative(request.initialUnits(), "El stock inicial no puede ser negativo");
        int minimumStock = requireNonNegative(request.minimumStock(), "El stock mínimo no puede ser negativo");
        String creationKey = requiredKey(request.idempotencyKey());

        StockEntity priorProduct = stockRepository.findByCreationKey(creationKey).orElse(null);
        if (priorProduct != null) {
            if (!priorProduct.getName().equalsIgnoreCase(name)
                    || !priorProduct.getStockType().equals(type)) {
                throw new RequestConflictException("La clave de creación ya se utilizó para otro producto");
            }
            return mapProduct(priorProduct);
        }
        if (stockRepository.existsByNameIgnoreCase(name)) {
            throw new RequestConflictException("Ya existe un producto con ese nombre");
        }

        StockEntity product = new StockEntity();
        product.setName(name);
        product.setNormalizedName(normalizeName(name));
        product.setDescription(normalizeDescription(request.description()));
        product.setStockType(type);
        product.setMinimumStock(minimumStock);
        product.setUds(initialUnits);
        product.setWarehouseUnits(initialUnits);
        product.setLaundryUnits(0);
        product.setActive(true);
        product.setCreationKey(creationKey);
        if (request.subcategoryId() != null) {
            validateSubcategory(request.subcategoryId());
            if (stockRepository.findBySubcategorySubcategoryId(request.subcategoryId()).isPresent()) {
                throw new RequestConflictException(
                        "La subcategoría ya está vinculada a otro producto de stock");
            }
            product.setSubcategory(subcategoryRepository.findById(request.subcategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("La subcategoría no existe")));
        }
        product = stockRepository.saveAndFlush(product);

        if (initialUnits > 0) {
            saveMovement(product, STOCK_ENTRY, initialUnits, null, null, actorUsername,
                    findWorker(actorUsername), null, creationKey,
                    0, 0, 0, initialUnits, 0, initialUnits);
        }
        return mapProduct(product);
    }

    /** Actualiza datos descriptivos y el mínimo sin alterar cantidades ni tipo de producto. */
    @Override
    public StockProductDto updateProduct(Long productId, StockUpdateRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de edición son obligatorios");
        }
        StockEntity product = findProductForUpdate(productId);
        String name = requiredText(request.name(), "El nombre del producto es obligatorio");
        int minimumStock = requireNonNegative(request.minimumStock(), "El stock mínimo no puede ser negativo");
        if (stockRepository.existsByNameIgnoreCaseAndProductIdNot(name, productId)) {
            throw new RequestConflictException("Ya existe un producto con ese nombre");
        }
        product.setName(name);
        product.setNormalizedName(normalizeName(name));
        product.setDescription(normalizeDescription(request.description()));
        product.setMinimumStock(minimumStock);
        return mapProduct(stockRepository.save(product));
    }

    /** Consulta los movimientos históricos de un producto, incluidos los productos desactivados. */
    @Override
    @Transactional(readOnly = true)
    public List<StockMovementDto> findMovements(Long productId) {
        if (!stockRepository.existsById(productId)) {
            throw new StockProductNotFoundException(productId);
        }
        return movementRepository.findAllByProductProductIdOrderByCreatedAtDescMovementIdDesc(productId)
                .stream().map(this::mapMovement).toList();
    }

    /** Aplica un movimiento con bloqueo de inventario y clave de idempotencia. */
    @Override
    public StockMovementDto createMovement(
            Long productId,
            StockMovementCreateRequestDto request,
            String actorUsername) {
        validateActor(actorUsername);
        validateMovementRequest(request);
        StockMovementEntity previousAttempt = movementRepository
                .findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (previousAttempt != null) {
            validateDuplicateAttempt(previousAttempt, productId, request);
            return mapMovement(previousAttempt);
        }

        WorkerEntity worker = findWorker(actorUsername);
        ServiceRequestEntity serviceRequest = null;
        if (request.serviceRequestId() != null) {
            serviceRequest = validateWorkerTask(request.serviceRequestId(), worker);
        }
        StockEntity product = findProductForUpdate(productId);
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new RequestConflictException("El producto está desactivado");
        }
        previousAttempt = movementRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (previousAttempt != null) {
            validateDuplicateAttempt(previousAttempt, productId, request);
            return mapMovement(previousAttempt);
        }

        int previousWarehouse = product.getWarehouseUnits();
        int previousLaundry = product.getLaundryUnits();
        int previousTotal = product.getUds();
        int quantity = request.quantity();
        int nextWarehouse = previousWarehouse;
        int nextLaundry = previousLaundry;
        int nextTotal = previousTotal;
        String type = request.movementType();

        switch (type) {
            case STOCK_ENTRY -> {
                nextWarehouse = addUnits(previousWarehouse, quantity);
                nextTotal = addUnits(previousTotal, quantity);
            }
            case TO_LAUNDRY -> {
                requireReusable(product);
                requireAvailable(product, previousWarehouse, quantity);
                nextWarehouse -= quantity;
                nextLaundry = addUnits(previousLaundry, quantity);
            }
            case FROM_LAUNDRY -> {
                requireReusable(product);
                requireAvailable(product, previousLaundry, quantity);
                nextLaundry -= quantity;
                nextWarehouse = addUnits(previousWarehouse, quantity);
            }
            case DISPOSAL -> {
                requireReusable(product);
                if (WAREHOUSE.equals(request.sourceState())) {
                    requireAvailable(product, previousWarehouse, quantity);
                    nextWarehouse -= quantity;
                } else {
                    requireAvailable(product, previousLaundry, quantity);
                    nextLaundry -= quantity;
                }
                nextTotal -= quantity;
            }
            default -> throw new IllegalArgumentException("Tipo de movimiento no permitido");
        }

        product.setWarehouseUnits(nextWarehouse);
        product.setLaundryUnits(nextLaundry);
        product.setUds(nextTotal);
        stockRepository.save(product);
        StockMovementEntity movement = saveMovement(product, type, quantity,
                request.sourceState(), request.reason(), actorUsername, worker, serviceRequest,
                request.idempotencyKey(), previousWarehouse, previousLaundry, previousTotal,
                nextWarehouse, nextLaundry, nextTotal);
        return mapMovement(movement);
    }

    /** Descuenta una entrega consumible al resolver la solicitud, dentro de su transacción. */
    @Override
    public void recordRequestConsumption(
            Long subcategoryId,
            Integer quantity,
            Long serviceRequestId,
            String actorUsername) {
        if (quantity == null || quantity < 1) {
            return;
        }
        validateActor(actorUsername);
        StockEntity product = stockRepository.findBySubcategorySubcategoryIdForUpdate(subcategoryId)
                .orElse(null);
        if (product == null || !CONSUMABLE.equals(product.getStockType())) {
            return;
        }
        if (movementRepository.findByServiceRequestServiceRequestIdAndMovementType(
                serviceRequestId, CONSUMPTION).isPresent()) {
            return;
        }
        int available = product.getUds();
        if (available < quantity) {
            throw new StockInsufficientException(product.getName(), available, quantity);
        }
        int next = available - quantity;
        product.setUds(next);
        product.setWarehouseUnits(next);
        stockRepository.save(product);
        saveMovement(product, CONSUMPTION, quantity, null, null, actorUsername,
                findWorker(actorUsername), requestRepository.getReferenceById(serviceRequestId),
                "request-consumption-" + serviceRequestId,
                available, 0, available, next, 0, next);
    }

    /** Verifica que una subcategoría exista y pertenezca a una categoría de servicio. */
    @Override
    public void validateSubcategory(Long subcategoryId) {
        if (subcategoryId == null || subcategoryId <= 0) {
            throw new IllegalArgumentException("La subcategoría no es válida");
        }
        RequestSubcategoryEntity subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new IllegalArgumentException("La subcategoría no existe"));
        if (!"SERVICE".equals(subcategory.getCategory().getType())) {
            throw new IllegalArgumentException(
                    "Solo se pueden vincular productos con subcategorías de tipo SERVICE");
        }
    }

    private StockEntity findProductForUpdate(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("El identificador del producto no es válido");
        }
        return stockRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new StockProductNotFoundException(productId));
    }

    private StockProductDto mapProduct(StockEntity product) {
        String status = stockStatus(product);
        RequestSubcategoryEntity subcategory = product.getSubcategory();
        return new StockProductDto(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getStockType(),
                product.getUds(),
                product.getWarehouseUnits(),
                product.getLaundryUnits(),
                product.getMinimumStock(),
                status,
                subcategory == null ? null : subcategory.getSubcategoryId(),
                subcategory == null ? null : subcategory.getName(),
                product.getActive());
    }

    private String stockStatus(StockEntity product) {
        int available = REUSABLE.equals(product.getStockType())
                ? product.getWarehouseUnits() : product.getUds();
        int minimum = product.getMinimumStock();
        if (available <= minimum) {
            return REORDER;
        }
        return (long) available * 4 > (long) minimum * 5 ? CORRECT : REORDER_SOON;
    }

    private StockMovementDto mapMovement(StockMovementEntity movement) {
        WorkerEntity worker = movement.getWorker();
        ServiceRequestEntity request = movement.getServiceRequest();
        return new StockMovementDto(
                movement.getMovementId(),
                movement.getProduct().getProductId(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getSourceState(),
                movement.getReason(),
                movement.getCreatedAt(),
                movement.getActorUsername(),
                worker == null ? null : worker.getWorkerId(),
                request == null ? null : request.getServiceRequestId(),
                movement.getPreviousWarehouse(),
                movement.getPreviousLaundry(),
                movement.getPreviousTotal(),
                movement.getNextWarehouse(),
                movement.getNextLaundry(),
                movement.getNextTotal());
    }

    private StockMovementEntity saveMovement(
            StockEntity product,
            String type,
            int quantity,
            String sourceState,
            String reason,
            String actorUsername,
            WorkerEntity worker,
            ServiceRequestEntity request,
            String idempotencyKey,
            int previousWarehouse,
            int previousLaundry,
            int previousTotal,
            int nextWarehouse,
            int nextLaundry,
            int nextTotal) {
        StockMovementEntity movement = new StockMovementEntity();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setSourceState(sourceState);
        movement.setReason(reason);
        movement.setCreatedAt(LocalDateTime.now());
        movement.setActorUsername(actorUsername);
        movement.setWorker(worker);
        movement.setServiceRequest(request);
        movement.setIdempotencyKey(idempotencyKey);
        movement.setPreviousWarehouse(previousWarehouse);
        movement.setPreviousLaundry(previousLaundry);
        movement.setPreviousTotal(previousTotal);
        movement.setNextWarehouse(nextWarehouse);
        movement.setNextLaundry(nextLaundry);
        movement.setNextTotal(nextTotal);
        return movementRepository.save(movement);
    }

    private void validateMovementRequest(StockMovementCreateRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del movimiento son obligatorios");
        }
        String type = request.movementType();
        if (!STOCK_ENTRY.equals(type) && !TO_LAUNDRY.equals(type)
                && !FROM_LAUNDRY.equals(type) && !DISPOSAL.equals(type)) {
            throw new IllegalArgumentException("Tipo de movimiento no permitido");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser positiva");
        }
        requiredKey(request.idempotencyKey());
        if (DISPOSAL.equals(type)) {
            if (!WAREHOUSE.equals(request.sourceState()) && !LAUNDRY.equals(request.sourceState())) {
                throw new IllegalArgumentException("Indica si las unidades proceden del almacén o lavandería");
            }
            requiredText(request.reason(), "Indica el motivo del desecho");
        } else if (request.sourceState() != null || request.reason() != null) {
            throw new IllegalArgumentException("El motivo y el estado de origen solo aplican al desecho");
        }
    }

    private void validateDuplicateAttempt(
            StockMovementEntity previous,
            Long productId,
            StockMovementCreateRequestDto request) {
        boolean matches = Objects.equals(previous.getProduct().getProductId(), productId)
                && Objects.equals(previous.getMovementType(), request.movementType())
                && Objects.equals(previous.getQuantity(), request.quantity())
                && Objects.equals(previous.getSourceState(), request.sourceState())
                && Objects.equals(previous.getReason(), request.reason())
                && Objects.equals(
                        previous.getServiceRequest() == null ? null : previous.getServiceRequest().getServiceRequestId(),
                        request.serviceRequestId());
        if (!matches) {
            throw new RequestConflictException("La clave de idempotencia ya se utilizó para otro movimiento");
        }
    }

    private ServiceRequestEntity validateWorkerTask(Long serviceRequestId, WorkerEntity worker) {
        if (worker == null) {
            throw new WorkerNotFoundException("desconocido");
        }
        ServiceRequestEntity request = requestRepository.findByIdForUpdate(serviceRequestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(serviceRequestId));
        if (!"IN_PROGRESS".equals(request.getStatus())
                || request.getWorker() == null
                || !Objects.equals(request.getWorker().getWorkerId(), worker.getWorkerId())) {
            throw new RequestConflictException(
                    "La operación solo puede registrarse en una tarea en curso asignada a este trabajador");
        }
        String categoryType = request.getCategory().getType();
        if (!"SERVICE".equals(categoryType)
                && !("INCIDENT".equals(categoryType) && "Limpieza".equals(request.getCategory().getName()))) {
            throw new RequestConflictException("La operación de inventario solo está disponible en Housekeeping");
        }
        return request;
    }

    private WorkerEntity findWorker(String username) {
        return workerRepository.findByUserUsername(username).orElse(null);
    }

    private void requireReusable(StockEntity product) {
        if (!REUSABLE.equals(product.getStockType())) {
            throw new RequestConflictException("Esta operación solo está disponible para productos reutilizables");
        }
    }

    private void requireAvailable(StockEntity product, int available, int requested) {
        if (available < requested) {
            throw new StockInsufficientException(product.getName(), available, requested);
        }
    }

    private int addUnits(int current, int addition) {
        if (addition > Integer.MAX_VALUE - current) {
            throw new IllegalArgumentException("La cantidad supera el límite de inventario permitido");
        }
        return current + addition;
    }

    private int requireNonNegative(Integer value, String message) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String normalizeProductType(String type) {
        if (!CONSUMABLE.equals(type) && !REUSABLE.equals(type)) {
            throw new IllegalArgumentException("El tipo de producto debe ser CONSUMABLE o REUSABLE");
        }
        return type;
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > 160) {
            throw new IllegalArgumentException("El nombre del producto no puede superar 160 caracteres");
        }
        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar 255 caracteres");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private String requiredKey(String key) {
        if (key == null || key.isBlank() || key.length() > 160) {
            throw new IllegalArgumentException("La clave de idempotencia es obligatoria y no puede superar 160 caracteres");
        }
        return key;
    }

    private void validateActor(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("No se ha podido identificar al usuario autenticado");
        }
    }
}
