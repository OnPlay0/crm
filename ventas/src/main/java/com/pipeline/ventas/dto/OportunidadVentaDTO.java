package com.pipeline.ventas.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pipeline.ventas.model.EstadoOportunidad;
import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OportunidadVentaDTO {

    private Long id;

    @JsonIgnore
    private Long userId;

    private String cliente;
    private String descripcion;
    private Double monto;
    private EstadoOportunidad estado;
    private LocalDate fechaCierreEstimada;
    private LocalDateTime fechaRegistro;
}