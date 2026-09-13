package com.cinema.service;

import com.cinema.dto.ProductCreateRequest;
import com.cinema.dto.ProductDTO;
import com.cinema.exception.ProductExceptions;
import com.cinema.model.Product;
import com.cinema.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j 
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        log.debug("Получение списка всех товаров из БД");
        List<Product> products = productRepository.findAll();
        log.debug("Из БД получено {} товаров", products.size());
        
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        log.debug("Поиск товара по id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Товар с id={} не найден", id);
                    return new ProductExceptions.NotFound(id);
                });
        log.debug("Товар с id={} найден: '{}'", id, product.getName());
        
        return convertToDTO(product);
    }

    public List<ProductDTO> searchProducts(String query) {
        log.debug("Поиск товаров по подстроке: '{}'", query);
        List<Product> found = productRepository.findByNameContainingIgnoreCase(query);
        log.debug("По запросу '{}' найдено {} товаров", query, found.size());
        
        return found.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductCreateRequest request) {
        log.debug("Создание товара с названием '{}'", request.getName());

        validateProduct(request);

        if (productRepository.existsByName(request.getName())) {
            log.warn("Попытка создать товар с уже существующим названием: '{}'", request.getName());
            throw new ProductExceptions.DuplicateName(request.getName());
        }

        Product product = new Product(
                request.getName(),
                request.getPrice()
        );

        Product saved = productRepository.save(product);
        log.info("Товар '{}' успешно создан с id={} (цена: {})",
                saved.getName(), saved.getId(), saved.getPrice());
        
        return convertToDTO(saved);
    }

    public ProductDTO updateProduct(Long id, ProductCreateRequest request) {
        log.debug("Обновление товара с id={}", id);

        validateProduct(request);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Невозможно обновить: товар с id={} не найден", id);
                    return new ProductExceptions.NotFound(id);
                });

        if (productRepository.existsByNameAndIdNot(request.getName(), id)) {
            log.warn("Попытка переименовать товар id={} в уже занятое название '{}'",
                    id, request.getName());
            throw new ProductExceptions.DuplicateName(request.getName());
        }

        log.debug("Обновление полей товара id={}: '{}' -> '{}', цена {} -> {}",
                id, product.getName(), request.getName(),
                product.getPrice(), request.getPrice());

        product.setName(request.getName());
        product.setPrice(request.getPrice());

        Product updated = productRepository.save(product);
        log.info("Товар с id={} успешно обновлён", id);
        
        return convertToDTO(updated);
    }

    public void deleteProduct(Long id) {
        log.debug("Удаление товара с id={}", id);

        if (!productRepository.existsById(id)) {
            log.warn("Невозможно удалить: товар с id={} не найден", id);
            throw new ProductExceptions.NotFound(id);
        }

        productRepository.deleteById(id);
        log.info("Товар с id={} успешно удалён", id);
    }

    private void validateProduct(ProductCreateRequest request) {
        BigDecimal price = request.getPrice();
        if (price == null || price.signum() <= 0) {
            log.warn("Некорректная цена товара: {}", price);
            throw new ProductExceptions.InvalidPrice(price);
        }
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getPrice()
        );
    }
}