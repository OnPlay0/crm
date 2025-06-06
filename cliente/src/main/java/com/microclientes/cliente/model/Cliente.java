package com.microclientes.cliente.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;

    @Enumerated(EnumType.STRING)
    private EstadoCliente estado;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

}
