package com.example.productservice.service;

import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductRequestDTO;
import com.example.productservice.dto.ProductResponseDTO;

import java.util.Map;

import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO create(ProductRequestDTO request);

    ProductResponseDTO getById(Long id);

    PagedResponse<ProductResponseDTO> search(String name, String category, Pageable pageable);

    ProductResponseDTO update(Long id, ProductRequestDTO request);

    Map<String, Object> delete(Long id);
}
