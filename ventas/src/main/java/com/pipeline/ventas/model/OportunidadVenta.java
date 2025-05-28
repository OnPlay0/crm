package com.pipeline.ventas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "oportunidades_venta")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OportunidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String cliente;
    private String descripcion;
    private Double monto;

    @Enumerated(EnumType.STRING)
    private EstadoOportunidad estado;

    private LocalDate fechaCierreEstimada;

    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
    }
}