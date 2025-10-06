package com.pipeline.ventas.repositories;

import com.pipeline.ventas.dto.CatalogItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {
    @GetMapping("/api/catalog/{id}")
    CatalogItemDTO getById(@PathVariable("id") Long id);

    @PostMapping("/api/catalog")
    CatalogItemDTO create(@RequestBody CatalogItemDTO dto);
}
