package com.example.productservice.service.impl;

import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.DuplicateResourceException;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateResourceException("A product with SKU '" + request.getSku() + "' already exists");
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponseDto(saved);
    }

    @Override
    public ProductResponseDTO getById(Long id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponseDto(product);
    }

    @Override
    public PagedResponse<ProductResponseDTO> search(String name, String category, Pageable pageable) {
        boolean hasName = StringUtils.hasText(name);
        boolean hasCategory = StringUtils.hasText(category);

        Page<Product> page;
        if (hasName && hasCategory) {
            page = productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(category, name, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (hasName) {
            page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return PagedResponse.from(page.map(productMapper::toResponseDto));
    }

    @Override
    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO request) {
        Product product = findProductOrThrow(id);

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("A product with SKU '" + request.getSku() + "' already exists");
        }

        productMapper.updateEntityFromDto(request, product);
        Product saved = productRepository.save(product);
        log.info("Updated product id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public Map<String, Object> delete(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(Boolean.FALSE);
        productRepository.save(product);
        log.info("Deleted product id={}", id);
        return Map.of("message", "Product deleted successfully");
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Product", id));
    }
}
