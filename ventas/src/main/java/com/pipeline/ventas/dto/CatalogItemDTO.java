package com.pipeline.ventas.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogItemDTO {
    private Long id;
    private String type;       // PRODUCT | SERVICE
    private String sku;
    private String name;
    private String description;
    private Double price;
    private Double cost;       // opcional
    private Integer stock;     // solo PRODUCT
    private Boolean active;
    private String category;
}
