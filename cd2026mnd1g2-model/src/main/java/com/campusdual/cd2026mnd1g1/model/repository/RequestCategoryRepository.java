package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.RequestCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RequestCategoryRepository extends JpaRepository<RequestCategoryEntity, Long> {
    List<RequestCategoryEntity> findByActiveTrueAndTypeOrderByCategoryId(String type);
}
