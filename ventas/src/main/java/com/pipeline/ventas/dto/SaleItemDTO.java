package com.pipeline.ventas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleItemDTO {

    // 👉 productId ya no es obligatorio: puede ser null en "venta rápida"
    private Long productId;

    @NotNull @Positive(message="quantity > 0")
    private Integer quantity;

    @NotNull @Positive(message="unitPrice > 0")
    private Double unitPrice;

    // 👇 estos son CLAVE para ventas rápidas
    private String sku;
    private String name;
    private String description;

    // true = SERVICE, false = PRODUCT
    private Boolean service;
}
