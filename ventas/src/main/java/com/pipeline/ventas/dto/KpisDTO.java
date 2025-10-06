package com.pipeline.ventas.dto;

import lombok.*;

// com.pipeline.ventas.dto.KpisDTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpisDTO {
    private long sales;
    private long items;
    private double revenue;
    private double avgTicket;
    private double pctServices;
}
