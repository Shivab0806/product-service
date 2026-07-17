package com.example.productservice.mapper;

import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Explicit, hand-written mapping (no reflection-based magic) keeps the
 * entity <-> DTO boundary easy to reason about and debug.
 */
@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto) {
        return Product.builder()
                .sku(dto.getSku().trim())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .category(dto.getCategory())
                .active(dto.getActive() == null || dto.getActive())
                .build();
    }

    /** Copies request fields onto an existing managed entity for updates. */
    public void updateEntityFromDto(ProductRequestDTO dto, Product product) {
        product.setSku(dto.getSku().trim());
        product.setName(dto.getName().trim());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
    }

    public ProductResponseDTO toResponseDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .active(product.getActive())
                .version(product.getVersion())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
