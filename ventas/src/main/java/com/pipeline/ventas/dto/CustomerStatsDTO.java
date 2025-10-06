package com.pipeline.ventas.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerStatsDTO {
    private Long customerId;
    private long sales;
    private double total;
}
