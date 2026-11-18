package com.campusdual.cd2026mnd1g1.model.service;

import com.campusdual.cd2026mnd1g1.api.dto.*;
import com.campusdual.cd2026mnd1g1.api.service.RequestService;
import com.campusdual.cd2026mnd1g1.model.entity.*;
import com.campusdual.cd2026mnd1g1.model.exception.ServiceRequestNotFoundException;
import com.campusdual.cd2026mnd1g1.model.exception.WorkerNotFoundException;
import com.campusdual.cd2026mnd1g1.model.exception.RequestConflictException;
import com.campusdual.cd2026mnd1g1.model.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RequestServiceImpl implements RequestService {
    private static final Integer DEMO_CABIN_NUMBER = 405;
    private static final String PENDING = "PENDING";
    private final CabinRepository cabinRepository;
    private final RequestCategoryRepository categoryRepository;
    private final RequestSubcategoryRepository subcategoryRepository;
    private final ServiceRequestRepository requestRepository;
    private final WorkerRepository workerRepository;

    public RequestServiceImpl(CabinRepository cabinRepository, RequestCategoryRepository categoryRepository,
                              RequestSubcategoryRepository subcategoryRepository, ServiceRequestRepository requestRepository,
                              WorkerRepository workerRepository) {
        this.cabinRepository = cabinRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.requestRepository = requestRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestCategoryDto> findCategories(String type) {
        if (!"INCIDENT".equals(type) && !"SERVICE".equals(type)) throw new IllegalArgumentException("El tipo debe ser INCIDENT o SERVICE");
        return categoryRepository.findByActiveTrueAndTypeOrderByCategoryId(type).stream().map(this::mapCategory).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestSubcategoryDto> findSubcategories(Long categoryId) {
        if (categoryId == null || categoryId <= 0) throw new IllegalArgumentException("La categoria no es valida");
        return subcategoryRepository.findByCategoryCategoryIdAndActiveTrueOrderBySubcategoryId(categoryId)
                .stream().map(this::mapSubcategory).toList();
    }

    @Override
    public ServiceRequestDto create(ServiceRequestCreateDto request) {
        if (request == null || request.categoryId() == null || request.subcategoryId() == null)
            throw new IllegalArgumentException("Categoria y subcategoria son obligatorias");
        if (request.quantity() != null && request.quantity() < 1)
            throw new IllegalArgumentException("La cantidad minima es 1");
        RequestCategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("La categoria no existe"));
        RequestSubcategoryEntity subcategory = subcategoryRepository.findById(request.subcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("La subcategoria no existe"));
        if (!Objects.equals(subcategory.getCategory().getCategoryId(), category.getCategoryId()))
            throw new IllegalArgumentException("La subcategoria no pertenece a la categoria");
        CabinEntity cabin = cabinRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay camarote ficticio configurado"));
        LocalDateTime now = LocalDateTime.now();
        ServiceRequestEntity entity = new ServiceRequestEntity();
        entity.setCabin(cabin); entity.setCategory(category); entity.setSubcategory(subcategory);
        entity.setQuantity(request.quantity()); entity.setPreferredDelivery(request.preferredDelivery());
        entity.setStatus(PENDING); entity.setCreatedAt(now); entity.setUpdatedAt(now);
        RequestStatusHistoryEntity history = new RequestStatusHistoryEntity();
        history.setServiceRequest(entity); history.setNewStatus(PENDING); history.setChangedAt(now);
        entity.getStatusHistory().add(history);
        return mapRequest(requestRepository.save(entity));
    }

    @Override
    public List<ServiceRequestDto> findAll() {
        return requestRepository.findByCabinCabinNumberOrderByCreatedAtDesc(DEMO_CABIN_NUMBER).stream()
                .map(this::mapRequest).toList();
    }

    @Override
    public ServiceRequestDto findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id no es valido");
        ServiceRequestEntity request = requestRepository.findById(id)
                .filter(r -> DEMO_CABIN_NUMBER.equals(r.getCabin().getCabinNumber()))
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
        return mapRequest(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerRequestDto> findUnassignedByDepartment(String department) {
        validateDepartment(department);
        return requestRepository.findUnassignedByDepartment(department).stream()
                .map(this::mapWorkerRequest).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerRequestDto> findAssignedByDepartment(String department, String username) {
        validateDepartment(department);
        WorkerEntity worker = findWorker(username);
        return requestRepository.findAssignedByDepartmentAndWorker(department, worker.getWorkerId()).stream()
                .map(this::mapWorkerRequest).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerRequestDto> findResolvedByDepartment(String department, String username) {
        validateDepartment(department);
        WorkerEntity worker = findWorker(username);
        return requestRepository.findResolvedByDepartmentAndWorker(department, worker.getWorkerId()).stream()
                .map(this::mapWorkerRequest).toList();
    }

    @Override
    public WorkerRequestDto assign(Long requestId, String department, String username) {
        validateDepartment(department);
        WorkerEntity worker = findWorker(username);
        ServiceRequestEntity request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
        validateDepartmentOfRequest(request, department);
        if (!PENDING.equals(request.getStatus()) || request.getWorker() != null) {
            throw new RequestConflictException("La solicitud ya ha sido asignada o no esta pendiente");
        }
        transition(request, "ASSIGNED");
        request.setWorker(worker);
        return mapWorkerRequest(requestRepository.save(request));
    }

    @Override
    public WorkerRequestDto start(Long requestId, String department, String username) {
        validateDepartment(department);
        WorkerEntity worker = findWorker(username);
        ServiceRequestEntity request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
        validateDepartmentOfRequest(request, department);
        validateWorker(request, worker);
        if (!"ASSIGNED".equals(request.getStatus())) {
            throw new RequestConflictException("La solicitud debe estar asignada antes de iniciarse");
        }
        transition(request, "IN_PROGRESS");
        return mapWorkerRequest(requestRepository.save(request));
    }

    @Override
    public WorkerRequestDto resolve(Long requestId, String department, String username) {
        validateDepartment(department);
        WorkerEntity worker = findWorker(username);
        ServiceRequestEntity request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
        validateDepartmentOfRequest(request, department);
        validateWorker(request, worker);
        if (!"IN_PROGRESS".equals(request.getStatus())) {
            throw new RequestConflictException("La solicitud debe estar en curso antes de finalizarse");
        }
        transition(request, "RESOLVED");
        return mapWorkerRequest(requestRepository.save(request));
    }

    private RequestCategoryDto mapCategory(RequestCategoryEntity category) {
        List<RequestSubcategoryDto> subcategories = category.getSubcategories().stream().filter(s -> Boolean.TRUE.equals(s.getActive()))
                .map(this::mapSubcategory).toList();
        return new RequestCategoryDto(category.getCategoryId(), category.getName(), category.getType(), category.getDescription(),
                category.getIcon(), category.getActive(), subcategories);
    }

    private void transition(ServiceRequestEntity request, String nextStatus) {
        LocalDateTime now = LocalDateTime.now();
        RequestStatusHistoryEntity history = new RequestStatusHistoryEntity();
        history.setServiceRequest(request);
        history.setPreviousStatus(request.getStatus());
        history.setNewStatus(nextStatus);
        history.setChangedAt(now);
        request.getStatusHistory().add(history);
        request.setStatus(nextStatus);
        request.setUpdatedAt(now);
    }
    private RequestSubcategoryDto mapSubcategory(RequestSubcategoryEntity subcategory) {
        return new RequestSubcategoryDto(subcategory.getSubcategoryId(), subcategory.getCategory().getCategoryId(),
                subcategory.getName(), subcategory.getDescription(), subcategory.getIcon(), subcategory.getActive());
    }
    private ServiceRequestDto mapRequest(ServiceRequestEntity request) {
        return new ServiceRequestDto(request.getServiceRequestId(), request.getCabin().getCabinNumber(), mapCategory(request.getCategory()),
                mapSubcategory(request.getSubcategory()), request.getQuantity(), request.getPreferredDelivery(), request.getStatus(),
                request.getCreatedAt(), request.getUpdatedAt(), request.getStatusHistory().stream()
                        .map(h -> new RequestStatusHistoryDto(h.getPreviousStatus(), h.getNewStatus(), h.getChangedAt())).toList());
    }

    private WorkerRequestDto mapWorkerRequest(ServiceRequestEntity request) {
        return new WorkerRequestDto(
                request.getServiceRequestId(),
                request.getCabin().getCabinNumber(),
                request.getSubcategory().getName(),
                request.getQuantity(),
                request.getStatus());
    }

    private WorkerEntity findWorker(String username) {
        if (username == null || username.isBlank()) {
            throw new WorkerNotFoundException("desconocido");
        }
        return workerRepository.findByUserUsername(username)
                .orElseThrow(() -> new WorkerNotFoundException(username));
    }

    private void validateDepartment(String department) {
        if (!"HOUSEKEEPING".equals(department) && !"MANTENIMIENTO".equals(department)) {
            throw new IllegalArgumentException("Departamento no valido");
        }
    }

    private void validateDepartmentOfRequest(ServiceRequestEntity request, String department) {
        if (!department.equals(getDepartment(request))) {
            throw new IllegalArgumentException("La solicitud no pertenece al departamento indicado");
        }
    }

    private void validateWorker(ServiceRequestEntity request, WorkerEntity worker) {
        if (request.getWorker() == null
                || !Objects.equals(request.getWorker().getWorkerId(), worker.getWorkerId())) {
            throw new RequestConflictException("La solicitud esta asignada a otro trabajador");
        }
    }

    private String getDepartment(ServiceRequestEntity request) {
        RequestCategoryEntity category = request.getCategory();
        if ("SERVICE".equals(category.getType())
                || ("INCIDENT".equals(category.getType()) && "Limpieza".equals(category.getName()))) {
            return "HOUSEKEEPING";
        }
        if ("INCIDENT".equals(category.getType())) {
            return "MANTENIMIENTO";
        }
        throw new IllegalArgumentException("La categoria no pertenece a un departamento de trabajadores");
    }
}
