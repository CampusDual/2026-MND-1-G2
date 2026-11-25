package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private StockEntity product;

    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "source_state", length = 20)
    private String sourceState;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "actor_username", nullable = false, length = 120)
    private String actorUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private WorkerEntity worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequestEntity serviceRequest;

    @Column(name = "idempotency_key", nullable = false, length = 160, unique = true)
    private String idempotencyKey;

    @Column(name = "previous_warehouse", nullable = false)
    private Integer previousWarehouse;

    @Column(name = "previous_laundry", nullable = false)
    private Integer previousLaundry;

    @Column(name = "previous_total", nullable = false)
    private Integer previousTotal;

    @Column(name = "next_warehouse", nullable = false)
    private Integer nextWarehouse;

    @Column(name = "next_laundry", nullable = false)
    private Integer nextLaundry;

    @Column(name = "next_total", nullable = false)
    private Integer nextTotal;

    public Long getMovementId() {
        return movementId;
    }

    public StockEntity getProduct() {
        return product;
    }

    public void setProduct(StockEntity product) {
        this.product = product;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSourceState() {
        return sourceState;
    }

    public void setSourceState(String sourceState) {
        this.sourceState = sourceState;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public WorkerEntity getWorker() {
        return worker;
    }

    public void setWorker(WorkerEntity worker) {
        this.worker = worker;
    }

    public ServiceRequestEntity getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequestEntity serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Integer getPreviousWarehouse() {
        return previousWarehouse;
    }

    public void setPreviousWarehouse(Integer previousWarehouse) {
        this.previousWarehouse = previousWarehouse;
    }

    public Integer getPreviousLaundry() {
        return previousLaundry;
    }

    public void setPreviousLaundry(Integer previousLaundry) {
        this.previousLaundry = previousLaundry;
    }

    public Integer getPreviousTotal() {
        return previousTotal;
    }

    public void setPreviousTotal(Integer previousTotal) {
        this.previousTotal = previousTotal;
    }

    public Integer getNextWarehouse() {
        return nextWarehouse;
    }

    public void setNextWarehouse(Integer nextWarehouse) {
        this.nextWarehouse = nextWarehouse;
    }

    public Integer getNextLaundry() {
        return nextLaundry;
    }

    public void setNextLaundry(Integer nextLaundry) {
        this.nextLaundry = nextLaundry;
    }

    public Integer getNextTotal() {
        return nextTotal;
    }

    public void setNextTotal(Integer nextTotal) {
        this.nextTotal = nextTotal;
    }
}
