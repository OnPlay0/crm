package com.pipeline.ventas.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SaleResponseDTO {
    private Long id;
    private LocalDate date;
    private String productName;
    private String productType;
    private String description;
    private Double total;
    private String estado;
    private LocalDate fechaCierreEstimada;
}
