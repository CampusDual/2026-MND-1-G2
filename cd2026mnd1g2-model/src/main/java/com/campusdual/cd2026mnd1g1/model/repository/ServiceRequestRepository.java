package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.ServiceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestEntity, Long> {
    List<ServiceRequestEntity> findByCabinCabinNumberOrderByCreatedAtDesc(Integer cabinNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT request FROM ServiceRequestEntity request WHERE request.serviceRequestId = :requestId")
    Optional<ServiceRequestEntity> findByIdForUpdate(@Param("requestId") Long requestId);

    @Query("""
            SELECT request FROM ServiceRequestEntity request
            WHERE request.status = 'PENDING'
              AND request.worker IS NULL
              AND (
                    (:department = 'HOUSEKEEPING'
                     AND (request.category.type = 'SERVICE'
                          OR (request.category.type = 'INCIDENT' AND request.category.name = 'Limpieza')))
                 OR (:department = 'MANTENIMIENTO'
                     AND request.category.type = 'INCIDENT'
                     AND request.category.name <> 'Limpieza')
              )
            ORDER BY request.createdAt DESC
            """)
    List<ServiceRequestEntity> findUnassignedByDepartment(@Param("department") String department);

    @Query("""
            SELECT request FROM ServiceRequestEntity request
            WHERE request.worker.workerId = :workerId
              AND request.status IN ('ASSIGNED', 'IN_PROGRESS')
              AND (
                    (:department = 'HOUSEKEEPING'
                     AND (request.category.type = 'SERVICE'
                          OR (request.category.type = 'INCIDENT' AND request.category.name = 'Limpieza')))
                 OR (:department = 'MANTENIMIENTO'
                     AND request.category.type = 'INCIDENT'
                     AND request.category.name <> 'Limpieza')
              )
            ORDER BY request.updatedAt DESC
            """)
    List<ServiceRequestEntity> findAssignedByDepartmentAndWorker(
            @Param("department") String department,
            @Param("workerId") Long workerId);

    @Query("""
            SELECT request FROM ServiceRequestEntity request
            WHERE request.worker.workerId = :workerId
              AND request.status = 'RESOLVED'
              AND (
                    (:department = 'HOUSEKEEPING'
                     AND (request.category.type = 'SERVICE'
                          OR (request.category.type = 'INCIDENT' AND request.category.name = 'Limpieza')))
                 OR (:department = 'MANTENIMIENTO'
                     AND request.category.type = 'INCIDENT'
                     AND request.category.name <> 'Limpieza')
              )
            ORDER BY request.updatedAt DESC
            """)
    List<ServiceRequestEntity> findResolvedByDepartmentAndWorker(
            @Param("department") String department,
            @Param("workerId") Long workerId);
}
