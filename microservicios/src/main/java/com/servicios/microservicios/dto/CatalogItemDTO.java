package com.servicios.microservicios.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogItemDTO {
    private Long id;
    @NotBlank private String type; // PRODUCT | SERVICE
    @Column(nullable = true)private String sku;
    @NotBlank private String name;
    private String description;
    @NotNull @Positive private Double price;
    @PositiveOrZero private Double cost;          // opcional
    @PositiveOrZero private Integer stock;        // solo PRODUCT
    private Boolean active;
    private String category;
}
