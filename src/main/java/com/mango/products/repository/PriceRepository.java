package com.mango.products.repository;

import com.mango.products.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    @Query("SELECT p FROM Price p WHERE p.product.id = :productId " +
           "AND p.initDate <= :date " +
           "AND (p.endDate IS NULL OR p.endDate >= :date)")
    Optional<Price> findByProductIdAndDate(@Param("productId") Long productId, @Param("date") LocalDate date);

    List<Price> findByProductIdOrderByInitDateAsc(Long productId);
}
