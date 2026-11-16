package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_status_history")
public class RequestStatusHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyRequestId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequestEntity serviceRequest;
    private String previousStatus;
    @Column(nullable = false) private String newStatus;
    @Column(nullable = false) private LocalDateTime changedAt;
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public void setServiceRequest(ServiceRequestEntity serviceRequest) { this.serviceRequest = serviceRequest; }
}
