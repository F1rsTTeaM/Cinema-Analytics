package com.cinema.repository;

import com.cinema.model.ProductSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductSaleRepository extends JpaRepository<ProductSale, Long> {

    List<ProductSale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<ProductSale> findByProductId(Long productId);
    
    @Query("SELECT SUM(ps.count) FROM ProductSale ps WHERE ps.product.id = :productId")
    Long countTotalSalesByProduct(@Param("productId") Long productId);
    
    @Query("SELECT ps.product.id, ps.product.name, SUM(ps.count), SUM(ps.product.price * ps.count) " +
           "FROM ProductSale ps " +
           "GROUP BY ps.product.id, ps.product.name")
    List<Object[]> findTopSellingProducts();
    
    @Query("SELECT ps.product.id, ps.product.name, SUM(ps.count), SUM(ps.product.price * ps.count) " +
           "FROM ProductSale ps " +
           "WHERE ps.saleDate BETWEEN :start AND :end " +
           "GROUP BY ps.product.id, ps.product.name")
    List<Object[]> getProductStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(ps.product.price * ps.count), 0) " +
           "FROM ProductSale ps " +
           "WHERE ps.saleDate BETWEEN :start AND :end")
    BigDecimal getTotalProductRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(ps.count), 0) " +
           "FROM ProductSale ps " +
           "WHERE ps.saleDate BETWEEN :start AND :end")
    Long getTotalProductsSold(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}