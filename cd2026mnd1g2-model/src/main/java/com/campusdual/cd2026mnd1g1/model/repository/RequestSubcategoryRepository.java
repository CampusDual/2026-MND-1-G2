package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.RequestSubcategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RequestSubcategoryRepository extends JpaRepository<RequestSubcategoryEntity, Long> {
    List<RequestSubcategoryEntity> findByCategoryCategoryIdAndActiveTrueOrderBySubcategoryId(Long categoryId);
}
