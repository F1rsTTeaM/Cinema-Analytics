package com.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.model.ProductSale;

@Repository
public interface ProductSaleRepository extends JpaRepository<ProductSale, Long> {
    List<ProductSale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
    List<ProductSale> findByProductId(Long productId);
    
    @Query("SELECT SUM(ps.count) FROM ProductSale ps WHERE ps.product.id = :productId")
    Long countTotalSalesByProduct(@Param("productId") Long productId);
    
    @Query("SELECT ps.product.id, SUM(ps.count) FROM ProductSale ps GROUP BY ps.product.id ORDER BY SUM(ps.count) DESC")
    List<Object[]> findTopSellingProducts();
}
