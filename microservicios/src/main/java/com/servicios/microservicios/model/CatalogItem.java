package com.servicios.microservicios.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="catalog_item",
        uniqueConstraints=@UniqueConstraint(name="uk_catalog_user_sku",columnNames={"user_id","sku"}),
        indexes={@Index(name="ix_catalog_user",columnList="user_id"),
                @Index(name="ix_catalog_sku", columnList="sku"),
                @Index(name="ix_catalog_type",columnList="type")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id",nullable=false) private Long userId;

    @Enumerated(EnumType.STRING) @Column(name="type",length=16,nullable=false)
    private ItemType type; // PRODUCT | SERVICE

    @Column(name="sku",length=80,nullable=false) private String sku;
    @Column(name="name",length=160,nullable=false) private String name;
    @Column(name="description",length=2000) private String description;

    @Column(name="price",nullable=false) private Double price;
    // Solo PRODUCT:
    @Column(name="cost")  private Double cost;
    @Column(name="stock") private Integer stock; // null en SERVICE

    @Column(name="active",nullable=false) private Boolean active = true;
    @Column(name="category",length=80) private String category;

    @Version private Long version;
    @Column(name="deleted_at") private LocalDateTime deletedAt;
}
