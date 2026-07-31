package com.cinema.service;

import com.cinema.dto.ProductSaleCreateRequest;
import com.cinema.dto.ProductSaleDTO;
import com.cinema.model.Product;
import com.cinema.model.ProductSale;
import com.cinema.repository.ProductRepository;
import com.cinema.repository.ProductSaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductSaleService {

    @Autowired
    private ProductSaleRepository productSaleRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductSaleDTO> getAllSales() {
        return productSaleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductSaleDTO createSale(ProductSaleCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (request.getCount() <= 0) {
            throw new RuntimeException("Количество должно быть больше 0");
        }

        ProductSale sale = new ProductSale();
        sale.setProduct(product);
        sale.setProductName(product.getName());
        sale.setProductPrice(product.getPrice());
        sale.setCount(request.getCount());
        sale.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getCount())));
        sale.setSaleDate(LocalDateTime.now());

        ProductSale savedSale = productSaleRepository.save(sale);
        return convertToDTO(savedSale);
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