package com.cinema.controller;

import com.cinema.dto.ProductSaleCreateRequest;
import com.cinema.dto.ProductSaleDTO;
import com.cinema.service.ProductSaleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@Slf4j 
public class ProductSaleController {

    @Autowired
    private ProductSaleService productSaleService;

    @GetMapping
    public ResponseEntity<List<ProductSaleDTO>> getAllSales() {
        log.info("GET /api/sales - запрос на получение списка всех продаж");
        List<ProductSaleDTO> sales = productSaleService.getAllSales();
        log.info("GET /api/sales - возвращено {} продаж", sales.size());
        
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/sales/stats - запрос статистики по продажам");

        log.debug("Расчёт статистики продаж");
        List<ProductSaleDTO> allSales = productSaleService.getAllSales();
        
        long totalSales = allSales.size();
        BigDecimal totalRevenue = allSales.stream()
                .map(ProductSaleDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Статистика: продаж={}, выручка={}", totalSales, totalRevenue);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSales", totalSales);
        stats.put("totalRevenue", totalRevenue);

        log.info("GET /api/sales/stats - продаж={}, выручка={}",
                totalSales, totalRevenue);
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<ProductSaleDTO> createSale(@Valid @RequestBody ProductSaleCreateRequest request) {
        log.info("POST /api/sales - запрос на создание продажи: productId={}, count={}",
                request.getProductId(), request.getCount());
        ProductSaleDTO created = productSaleService.createSale(request);
        log.info("POST /api/sales - продажа успешно создана с id={}, сумма={}",
                created.getId(), created.getTotalAmount());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}