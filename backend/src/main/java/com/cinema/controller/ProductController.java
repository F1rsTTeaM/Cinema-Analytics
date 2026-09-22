package com.cinema.controller;

import com.cinema.dto.ProductCreateRequest;
import com.cinema.dto.ProductDTO;
import com.cinema.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j 
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products - запрос на получение списка всех товаров");
        List<ProductDTO> products = productService.getAllProducts();
        log.info("GET /api/products - возвращено {} товаров", products.size());
        
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - запрос на получение товара по id", id);
        ProductDTO product = productService.getProductById(id);
        log.info("GET /api/products/{} - товар найден: '{}'", id, product.getName());
        
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        log.info("GET /api/products/search - поиск товаров по запросу: '{}'", query);
        List<ProductDTO> result = productService.searchProducts(query);
        log.info("GET /api/products/search - по запросу '{}' найдено {} товаров",
                query, result.size());
        
                return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("POST /api/products - запрос на создание товара: '{}' (цена: {})",
                request.getName(), request.getPrice());
        ProductDTO created = productService.createProduct(request);
        log.info("POST /api/products - товар успешно создан с id={}", created.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("PUT /api/products/{} - запрос на обновление товара: '{}'",
                id, request.getName());
        ProductDTO updated = productService.updateProduct(id, request);
        log.info("PUT /api/products/{} - товар успешно обновлён", id);
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - запрос на удаление товара", id);
        productService.deleteProduct(id);
        log.info("DELETE /api/products/{} - товар успешно удалён", id);
        
        return ResponseEntity.noContent().build();
    }
}