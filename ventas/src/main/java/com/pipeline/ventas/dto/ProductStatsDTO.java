package com.pipeline.ventas.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductStatsDTO {
    private String sku;
    private String name;
    private long quantity;
    private double total;
}

