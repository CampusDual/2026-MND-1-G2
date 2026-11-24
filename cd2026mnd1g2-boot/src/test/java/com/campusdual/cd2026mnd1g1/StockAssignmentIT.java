package com.campusdual.cd2026mnd1g1;

import com.campusdual.cd2026mnd1g1.api.service.RequestService;
import com.campusdual.cd2026mnd1g1.model.entity.CabinEntity;
import com.campusdual.cd2026mnd1g1.model.entity.RequestSubcategoryEntity;
import com.campusdual.cd2026mnd1g1.model.entity.ServiceRequestEntity;
import com.campusdual.cd2026mnd1g1.model.entity.StockEntity;
import com.campusdual.cd2026mnd1g1.model.exception.RequestConflictException;
import com.campusdual.cd2026mnd1g1.model.exception.StockInsufficientException;
import com.campusdual.cd2026mnd1g1.model.repository.CabinRepository;
import com.campusdual.cd2026mnd1g1.model.repository.RequestSubcategoryRepository;
import com.campusdual.cd2026mnd1g1.model.repository.ServiceRequestRepository;
import com.campusdual.cd2026mnd1g1.model.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StockAssignmentIT {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestSubcategoryRepository subcategoryRepository;

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private StockRepository stockRepository;

    private RequestSubcategoryEntity serviceSubcategory;
    private RequestSubcategoryEntity incidentSubcategory;
    private CabinEntity cabin;

    @BeforeEach
    void setUp() {
        serviceSubcategory = subcategoryRepository.findAll().stream()
                .filter(subcategory -> "SERVICE".equals(subcategory.getCategory().getType()))
                .findFirst()
                .orElseThrow();
        incidentSubcategory = subcategoryRepository.findAll().stream()
                .filter(subcategory -> "INCIDENT".equals(subcategory.getCategory().getType()))
                .findFirst()
                .orElseThrow();
        cabin = cabinRepository.findAll().getFirst();

        StockEntity stock = stockRepository.findBySubcategorySubcategoryId(
                        serviceSubcategory.getSubcategoryId())
                .orElseThrow();
        stock.setUds(500);
        stockRepository.saveAndFlush(stock);
    }

    @Test
    void assignsRequestAndDecrementsStockFrom500To497() {
        ServiceRequestEntity request = createRequest(serviceSubcategory, 3);

        requestService.assign(request.getServiceRequestId(), "HOUSEKEEPING", "admin");

        StockEntity stock = stockRepository.findBySubcategorySubcategoryId(
                        serviceSubcategory.getSubcategoryId())
                .orElseThrow();
        assertThat(stock.getUds()).isEqualTo(497);
    }

    @Test
    void rejectsInsufficientStockWithoutChangingRequestOrStock() {
        StockEntity stock = stockRepository.findBySubcategorySubcategoryId(
                        serviceSubcategory.getSubcategoryId())
                .orElseThrow();
        stock.setUds(2);
        stockRepository.saveAndFlush(stock);
        ServiceRequestEntity request = createRequest(serviceSubcategory, 3);

        assertThatThrownBy(() -> requestService.assign(
                        request.getServiceRequestId(), "HOUSEKEEPING", "admin"))
                .isInstanceOf(StockInsufficientException.class)
                .hasMessageContaining("disponibles 2")
                .hasMessageContaining("solicitadas 3");

        assertThat(requestRepository.findById(request.getServiceRequestId()).orElseThrow().getStatus())
                .isEqualTo("PENDING");
        assertThat(stockRepository.findBySubcategorySubcategoryId(
                        serviceSubcategory.getSubcategoryId()).orElseThrow().getUds())
                .isEqualTo(2);
    }

    @Test
    void assignsIncidentRequestWithoutStockProduct() {
        ServiceRequestEntity request = createRequest(incidentSubcategory, 3);

        requestService.assign(request.getServiceRequestId(), "MANTENIMIENTO", "admin");

        assertThat(requestRepository.findById(request.getServiceRequestId()).orElseThrow().getStatus())
                .isEqualTo("ASSIGNED");
        assertThat(stockRepository.findBySubcategorySubcategoryId(
                        incidentSubcategory.getSubcategoryId()))
                .isEmpty();
    }

    @Test
    void doesNotDecrementStockTwiceWhenAlreadyAssigned() {
        ServiceRequestEntity request = createRequest(serviceSubcategory, 3);
        requestService.assign(request.getServiceRequestId(), "HOUSEKEEPING", "admin");

        assertThatThrownBy(() -> requestService.assign(
                        request.getServiceRequestId(), "HOUSEKEEPING", "admin"))
                .isInstanceOf(RequestConflictException.class);

        assertThat(stockRepository.findBySubcategorySubcategoryId(
                        serviceSubcategory.getSubcategoryId()).orElseThrow().getUds())
                .isEqualTo(497);
    }

    private ServiceRequestEntity createRequest(RequestSubcategoryEntity subcategory, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        ServiceRequestEntity request = new ServiceRequestEntity();
        request.setCabin(cabin);
        request.setCategory(subcategory.getCategory());
        request.setSubcategory(subcategory);
        request.setQuantity(quantity);
        request.setPreferredDelivery("ASAP");
        request.setStatus("PENDING");
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        return requestRepository.saveAndFlush(request);
    }
}
