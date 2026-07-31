package com.cinema.controller;

import com.cinema.dto.ProductSaleCreateRequest;
import com.cinema.dto.ProductSaleDTO;
import com.cinema.service.ProductSaleService;
import jakarta.validation.Valid;
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
public class ProductSaleController {

    @Autowired
    private ProductSaleService productSaleService;

    @GetMapping
    public ResponseEntity<List<ProductSaleDTO>> getAllSales() {
        return ResponseEntity.ok(productSaleService.getAllSales());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<ProductSaleDTO> allSales = productSaleService.getAllSales();
        
        long totalSales = allSales.size();
        BigDecimal totalRevenue = allSales.stream()
                .map(ProductSaleDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSales", totalSales);
        stats.put("totalRevenue", totalRevenue);
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<ProductSaleDTO> createSale(@Valid @RequestBody ProductSaleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productSaleService.createSale(request));
    }
}