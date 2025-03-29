package com.micro.leads.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leads")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String telefono;
    private String empresa;

    @Enumerated(EnumType.STRING)
    private FuenteLead fuente;

    @Enumerated(EnumType.STRING)
    private EstadoLead estado;

    private LocalDate fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDate.now();
        if (this.estado == null) {
            this.estado = EstadoLead.NUEVO;
        }
    }
}
