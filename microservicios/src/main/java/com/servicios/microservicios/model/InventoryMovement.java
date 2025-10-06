package com.servicios.microservicios.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="inventory_movement",
        indexes=@Index(name="ix_mov_user_item",columnList="user_id,item_id,created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryMovement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(name="user_id",nullable=false) private Long userId;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="item_id",nullable=false)
    private CatalogItem item;

    @Enumerated(EnumType.STRING) @Column(name="type",length=16,nullable=false)
    private MovementType type; // IN | OUT | ADJUST

    @Column(name="quantity",nullable=false) private Integer quantity; // +IN, −OUT
    @Column(name="reason",length=160) private String reason;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
