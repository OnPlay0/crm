package com.pipeline.ventas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "oportunidades_venta")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OportunidadVenta { // Cambio de "VentaOportunidad" a "OportunidadVenta"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;
    private String descripcion;
    private Double monto;

    @Enumerated(EnumType.STRING)
    private EstadoOportunidad estado;

    private LocalDate fechaCreacion;
    private LocalDate fechaCierreEstimada;
}
