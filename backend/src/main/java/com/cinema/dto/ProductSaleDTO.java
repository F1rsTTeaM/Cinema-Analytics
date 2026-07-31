package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSaleDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer count;
    private BigDecimal productPrice;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
}