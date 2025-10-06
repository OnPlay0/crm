package com.pipeline.ventas.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleDTO {
    private Long id;
    private LocalDate date;
    private Long customerId;
    private String notes;
    private List<SaleItemDTO> items;
    private Double total;
}
