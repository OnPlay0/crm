    package com.pipeline.ventas.dto;

    import lombok.*;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public class SalesTotalsDTO {
        private long sales;
        private long items;
        private double revenue;
        private double avgTicket;
    }
