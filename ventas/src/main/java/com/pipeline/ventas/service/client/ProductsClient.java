package com.pipeline.ventas.service.client;

import lombok.*;

public interface ProductsClient {

    ProductSnapshot getById(Long productId);

    void stockOut(Long productId, int quantity, String reason);

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class ProductSnapshot {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private boolean service;
    }
}
