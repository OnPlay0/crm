package com.pipeline.ventas.dto;

import lombok.*;

import java.time.LocalDate;

// com.pipeline.ventas.dto.DailyRevenueDTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDTO {
    private LocalDate date;
    private double revenue;
}
