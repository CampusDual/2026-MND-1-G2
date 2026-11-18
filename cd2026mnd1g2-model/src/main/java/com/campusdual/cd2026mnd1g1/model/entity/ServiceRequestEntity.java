package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_requests")
public class ServiceRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceRequestId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cabin_id", nullable = false)
    private CabinEntity cabin;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false)
    private RequestCategoryEntity category;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "subcategory_id", nullable = false)
    private RequestSubcategoryEntity subcategory;
    private Integer quantity;
    private String preferredDelivery;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "worker_id", nullable = true)
    private WorkerEntity worker;
    @OneToMany(mappedBy = "serviceRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RequestStatusHistoryEntity> statusHistory = new ArrayList<>();
    public Long getServiceRequestId() { return serviceRequestId; }
    public CabinEntity getCabin() { return cabin; }
    public void setCabin(CabinEntity cabin) { this.cabin = cabin; }
    public RequestCategoryEntity getCategory() { return category; }
    public void setCategory(RequestCategoryEntity category) { this.category = category; }
    public RequestSubcategoryEntity getSubcategory() { return subcategory; }
    public void setSubcategory(RequestSubcategoryEntity subcategory) { this.subcategory = subcategory; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPreferredDelivery() { return preferredDelivery; }
    public void setPreferredDelivery(String preferredDelivery) { this.preferredDelivery = preferredDelivery; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public WorkerEntity getWorker() { return worker; }
    public void setWorker(WorkerEntity worker) { this.worker = worker; }
    public List<RequestStatusHistoryEntity> getStatusHistory() { return statusHistory; }
}
