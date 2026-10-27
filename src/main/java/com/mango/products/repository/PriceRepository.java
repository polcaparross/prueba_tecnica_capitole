package com.mango.products.repository;

import com.mango.products.model.Price;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    @Query("SELECT p FROM Price p WHERE p.product.id = :productId " +
           "AND p.initDate <= :date " +
           "AND (p.endDate IS NULL OR p.endDate >= :date) " +
           "ORDER BY p.initDate DESC")
    List<Price> findValidPrice(@Param("productId") Long productId, 
                               @Param("date") LocalDate date, 
                               Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM Price p WHERE p.product.id = :productId " +
           "AND (:newInit <= p.endDate OR p.endDate IS NULL) " +
           "AND (:newEnd >= p.initDate OR :newEnd IS NULL)")
    boolean existsOverlappingPrice(@Param("productId") Long productId, 
                                   @Param("newInit") LocalDate newInit, 
                                   @Param("newEnd") LocalDate newEnd);

    List<Price> findByProductIdOrderByInitDateAsc(Long productId);
}
