package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSaleStatsDTO {
    private Long productId;
    private String productName;
    private Long totalCount;
    private BigDecimal totalRevenue;
    private BigDecimal price;
}