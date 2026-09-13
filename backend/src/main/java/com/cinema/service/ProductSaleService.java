package com.cinema.service;

import com.cinema.dto.ProductSaleCreateRequest;
import com.cinema.dto.ProductSaleDTO;
import com.cinema.exception.ProductSaleExceptions;
import com.cinema.model.Product;
import com.cinema.model.ProductSale;
import com.cinema.repository.ProductRepository;
import com.cinema.repository.ProductSaleRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j 
public class ProductSaleService {

    @Autowired
    private ProductSaleRepository productSaleRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductSaleDTO> getAllSales() {
        log.debug("Получение списка всех продаж из БД");
        List<ProductSale> sales = productSaleRepository.findAll();
        log.debug("Из БД получено {} продаж", sales.size());
        
        return sales.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductSaleDTO createSale(ProductSaleCreateRequest request) {
        log.debug("Создание продажи: productId={}, count={}",
                request.getProductId(), request.getCount());

        if (request.getCount() == null || request.getCount() <= 0) {
            log.warn("Некорректное количество товара в продаже: {}", request.getCount());
            throw new ProductSaleExceptions.InvalidCount(request.getCount());
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Товар с id={} не найден при создании продажи",
                            request.getProductId());
                    return new ProductSaleExceptions.ProductNotFound(request.getProductId());
                });

        ProductSale sale = new ProductSale();
        sale.setProduct(product);
        sale.setProductName(product.getName());
        sale.setProductPrice(product.getPrice());
        sale.setCount(request.getCount());
        sale.setTotalAmount(product.getPrice()
                .multiply(BigDecimal.valueOf(request.getCount())));
        sale.setSaleDate(LocalDateTime.now());

        ProductSale saved = productSaleRepository.save(sale);
        log.info("Продажа успешно создана: id={}, товар='{}', количество={}, сумма={}",
                saved.getId(), saved.getProductName(), saved.getCount(), saved.getTotalAmount());
        
        return convertToDTO(saved);
    }

    private ProductSaleDTO convertToDTO(ProductSale sale) {
        return new ProductSaleDTO(
            sale.getId(),
            sale.getProduct().getId(),
            sale.getProductName(),
            sale.getCount(),
            sale.getProductPrice(),
            sale.getTotalAmount(),
            sale.getSaleDate()
        );
    }
}