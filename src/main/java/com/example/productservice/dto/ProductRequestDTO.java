package com.example.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Inbound payload for creating/updating a Product.
 * Kept separate from the entity so the persistence model can evolve
 * independently of the public API contract.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "SKU must not be blank")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,64}$", message = "SKU must be 3-64 chars: letters, digits, '-' or '_'")
    private String sku;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price may have at most 10 integer and 2 fraction digits")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private Boolean active;
}
