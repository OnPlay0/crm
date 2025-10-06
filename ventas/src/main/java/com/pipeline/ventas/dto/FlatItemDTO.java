package com.pipeline.ventas.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlatItemDTO {
    private LocalDate date;
    private Long saleId;
    private String sku;
    private String product;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private Long customerId;
}
