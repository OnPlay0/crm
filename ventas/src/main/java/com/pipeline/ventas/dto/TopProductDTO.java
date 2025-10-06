package com.pipeline.ventas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductDTO {
    private String sku;
    private String name;
    private long totalQty;
    private double totalRevenue;
}