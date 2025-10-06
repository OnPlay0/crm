package com.pipeline.ventas.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlySummaryDTO {
    private long sales;        // number of sales in the month
    private long items;        // number of items
    private double revenue;    // total revenue
}
