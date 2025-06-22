package com.example.productdashboard.controller;

import com.example.productdashboard.exception.ProductNotFoundException;
import com.example.productdashboard.model.Product;
import com.example.productdashboard.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository = new ProductRepository();

    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "id,asc") String sort) {

        List<Product> filtered = productRepository.findAll().stream()
                .filter(p -> category == null || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        Comparator<Product> comparator = Comparator.comparing(Product::getId);
        if (sort.startsWith("price")) {
            comparator = Comparator.comparing(Product::getPrice);
        }
        if (sort.endsWith("desc")) {
            comparator = comparator.reversed();
        }

        return filtered.stream()
                .sorted(comparator)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
    }

    @PostMapping
    public Product addProduct(@Valid @RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        if (!productRepository.exists(id)) {
            throw new ProductNotFoundException("Product not found with id " + id);
        }
        product.setId(id);
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.exists(id)) {
            throw new ProductNotFoundException("Product not found with id " + id);
        }
        productRepository.delete(id);
        return ResponseEntity.noContent().build();
    }
}
