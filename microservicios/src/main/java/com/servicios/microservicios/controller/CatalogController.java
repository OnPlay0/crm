package com.servicios.microservicios.controller;


import com.servicios.microservicios.dto.CatalogItemDTO;
import com.servicios.microservicios.service.CatalogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.servicios.microservicios.dto.ImportResult;

import java.io.IOException;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService service;

    @PostMapping
    public CatalogItemDTO create(@RequestBody @jakarta.validation.Valid CatalogItemDTO dto){
        return service.create(dto);
    }

    @GetMapping
    public Page<CatalogItemDTO> search(@RequestParam(required=false) String q,
                                       @RequestParam(required=false) String type, // PRODUCT|SERVICE
                                       @RequestParam(required=false) String category,
                                       @RequestParam(required=false) Boolean active,
                                       @ParameterObject Pageable pageable) {
        return service.search(q, type, category, active, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){ service.delete(id); }

    // ---------- Compatibilidad con Ventas ----------
    @GetMapping("/{id}/snapshot")
    public CatalogService.ProductSnapshot snapshot(@PathVariable Long id){
        return service.getById(id);
    }

    @PostMapping("/{id}/stock/out")
    public void stockOut(@PathVariable Long id, @RequestParam int qty, @RequestParam(required=false) String reason){
        service.stockOut(id, qty, reason==null? "SALE" : reason);
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=servicios.xlsx");
        service.exportServiciosExcel(response.getOutputStream());
    }

    @PostMapping("/import/excel")
    public ImportResult importExcel(@RequestParam("file") MultipartFile file) throws IOException { // 👈 USA EL DTO
        return service.importServiciosExcel(file);
    }

    // CatalogController
    @PutMapping("/{id}")
    public CatalogItemDTO update(@PathVariable Long id,
                                 @RequestBody @jakarta.validation.Valid CatalogItemDTO dto) {
        return service.update(id, dto);
    }



}
