package com.example.productservice.service;

import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.DuplicateResourceException;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequestDTO requestDTO;
    private Product entity;

    @BeforeEach
    void setUp() {
        requestDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Wireless Mouse")
                .description("Ergonomic wireless mouse")
                .price(new BigDecimal("29.99"))
                .quantity(100)
                .category("Electronics")
                .active(true)
                .build();

        entity = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Wireless Mouse")
                .price(new BigDecimal("29.99"))
                .quantity(100)
                .category("Electronics")
                .active(true)
                .version(0L)
                .build();
    }

    @Test
    void create_savesAndReturnsProduct_whenSkuIsUnique() {
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productMapper.toEntity(requestDTO)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponseDto(entity)).thenReturn(
                ProductResponseDTO.builder().id(1L).sku("SKU-001").name("Wireless Mouse").build());

        ProductResponseDTO result = productService.create(requestDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("SKU-001");
        verify(productRepository).save(entity);
    }

    @Test
    void create_throwsDuplicateResourceException_whenSkuAlreadyExists() {
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU-001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_removesProduct_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));

        productService.delete(1L);

        verify(productRepository).delete(entity);
    }
}
