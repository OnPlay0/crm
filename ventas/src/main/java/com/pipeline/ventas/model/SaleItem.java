package com.pipeline.ventas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sale_item", indexes = {
        @Index(name = "ix_sitem_sale", columnList = "sale_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Snapshots (stable historical reporting)
    private String skuSnapshot;
    private String nameSnapshot;
    @Column(length = 2000)
    private String descriptionSnapshot;

    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;

    private Boolean service;


    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
