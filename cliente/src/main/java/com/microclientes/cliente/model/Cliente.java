package com.microclientes.cliente.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clientes",
        uniqueConstraints = @UniqueConstraint(name="uk_cliente_user_email", columnNames={"user_id","email"}),
        indexes = {
                @Index(name="ix_cliente_user",  columnList="user_id"),
                @Index(name="ix_cliente_email", columnList="email")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Cliente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Email
    @Column(name="email", length=120 /*, nullable=false si lo querés obligatorio */)
    private String email;

    @Column(name="nombre",  length=80)
    private String nombre;

    @Column(name="apellido", length=80)
    private String apellido;

    @Column(name="telefono", length=20)
    private String telefono;

    @Column(name="direccion", length=160)
    private String direccion;

    @Column(name="fecha_registro")
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name="estado", length=12, nullable=false)
    private EstadoCliente estado = EstadoCliente.PROSPECTO;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name="created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name="updated_by")
    private Long updatedBy;

    // Lo mantenemos por si querés soft delete manual
    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name="version")
    private Long version;
}
