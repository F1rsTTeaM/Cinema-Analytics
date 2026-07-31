package com.cinema.service;

import com.cinema.dto.ProductCreateRequest;
import com.cinema.dto.ProductDTO;
import com.cinema.model.Product;
import com.cinema.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        return convertToDTO(product);
    }

    public List<ProductDTO> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductCreateRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Товар с таким названием уже существует");
        }

        Product product = new Product(
            request.getName(),
            request.getPrice()
        );

        return convertToDTO(productRepository.save(product));
    }

    public ProductDTO updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (productRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Товар с таким названием уже существует");
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());

        return convertToDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Товар не найден");
        }
        productRepository.deleteById(id);
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getPrice()
        );
    }
}