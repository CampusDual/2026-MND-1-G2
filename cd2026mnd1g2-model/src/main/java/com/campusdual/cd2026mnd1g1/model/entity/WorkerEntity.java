package com.campusdual.cd2026mnd1g1.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que relaciona un usuario del sistema con su perfil de trabajador.
 */
@Entity
@Table(name = "workers")
public class WorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workerId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY)
    private List<ServiceRequestEntity> serviceRequests = new ArrayList<>();

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<ServiceRequestEntity> getServiceRequests() {
        return serviceRequests;
    }
}
