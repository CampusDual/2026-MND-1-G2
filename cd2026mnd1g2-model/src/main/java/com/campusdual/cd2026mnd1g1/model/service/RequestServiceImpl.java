package com.campusdual.cd2026mnd1g1.model.service;

import com.campusdual.cd2026mnd1g1.api.dto.*;
import com.campusdual.cd2026mnd1g1.api.service.RequestService;
import com.campusdual.cd2026mnd1g1.model.entity.*;
import com.campusdual.cd2026mnd1g1.model.exception.ServiceRequestNotFoundException;
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

    public RequestServiceImpl(CabinRepository cabinRepository, RequestCategoryRepository categoryRepository,
                              RequestSubcategoryRepository subcategoryRepository, ServiceRequestRepository requestRepository) {
        this.cabinRepository = cabinRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.requestRepository = requestRepository;
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
                .map(this::advanceDemoStatus).map(this::mapRequest).toList();
    }

    @Override
    public ServiceRequestDto findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("El id no es valido");
        ServiceRequestEntity request = requestRepository.findById(id)
                .filter(r -> DEMO_CABIN_NUMBER.equals(r.getCabin().getCabinNumber()))
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
        return mapRequest(advanceDemoStatus(request));
    }

    private RequestCategoryDto mapCategory(RequestCategoryEntity category) {
        List<RequestSubcategoryDto> subcategories = category.getSubcategories().stream().filter(s -> Boolean.TRUE.equals(s.getActive()))
                .map(this::mapSubcategory).toList();
        return new RequestCategoryDto(category.getCategoryId(), category.getName(), category.getType(), category.getDescription(),
                category.getIcon(), category.getActive(), subcategories);
    }

    private ServiceRequestEntity advanceDemoStatus(ServiceRequestEntity request) {
        String nextStatus = switch (request.getStatus()) {
            case "PENDING" -> "ASSIGNED";
            case "ASSIGNED" -> "IN_PROGRESS";
            case "IN_PROGRESS" -> "RESOLVED";
            default -> null;
        };
        if (nextStatus == null) return request;
        LocalDateTime now = LocalDateTime.now();
        RequestStatusHistoryEntity history = new RequestStatusHistoryEntity();
        history.setServiceRequest(request);
        history.setPreviousStatus(request.getStatus());
        history.setNewStatus(nextStatus);
        history.setChangedAt(now);
        request.getStatusHistory().add(history);
        request.setStatus(nextStatus);
        request.setUpdatedAt(now);
        return requestRepository.save(request);
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
}
