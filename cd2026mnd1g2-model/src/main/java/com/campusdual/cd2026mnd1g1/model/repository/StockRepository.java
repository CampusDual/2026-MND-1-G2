package com.campusdual.cd2026mnd1g1.model.repository;

import com.campusdual.cd2026mnd1g1.model.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, Long> {

    @Query("""
            SELECT stock FROM StockEntity stock
            JOIN FETCH stock.subcategory subcategory
            JOIN FETCH subcategory.category category
            WHERE category.type = 'SERVICE'
            ORDER BY stock.name
            """)
    List<StockEntity> findServiceProducts();

    @Query("""
            SELECT COUNT(stock.productId) FROM StockEntity stock
            JOIN stock.subcategory subcategory
            JOIN subcategory.category category
            WHERE category.type = 'SERVICE'
            """)
    Long countServiceProducts();

    Optional<StockEntity> findBySubcategorySubcategoryId(Long subcategoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT stock FROM StockEntity stock
            WHERE stock.subcategory.subcategoryId = :subcategoryId
            """)
    Optional<StockEntity> findBySubcategorySubcategoryIdForUpdate(
            @Param("subcategoryId") Long subcategoryId);
}
