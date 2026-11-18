package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<WorkerEntity, Long> {

    /**
     * Busca el perfil de trabajador asociado al nombre de usuario autenticado.
     *
     * @param username nombre de usuario autenticado
     * @return perfil de trabajador si existe
     */
    Optional<WorkerEntity> findByUserUsername(String username);
}
