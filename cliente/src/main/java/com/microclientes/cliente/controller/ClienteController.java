package com.microclientes.cliente.controller;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.model.EstadoCliente;
import com.microclientes.cliente.service.ClienteService;
import com.microclientes.cliente.util.ExcelExporter;
import com.microclientes.cliente.util.SecurityUtils;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "API de gestión de clientes")
public class ClienteController {

    private final ClienteService service;

    // ClienteController.java
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> get(@PathVariable Long id, HttpServletRequest req) {
        ClienteDTO dto = service.get(id);
        long v = dto.getVersion() == null ? 0L : dto.getVersion();
        String etag = "\"" + dto.getId() + "-" + v + "\"";
        String inm = req.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (etag.equals(inm)) return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        return ResponseEntity.ok().eTag(etag).body(dto);
    }


    @PostMapping
    public ResponseEntity<ClienteDTO> create(@Valid @RequestBody ClienteDTO dto, HttpServletRequest req) {
        var created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, req.getRequestURL().append("/").append(created.getId()).toString())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        var updated = service.update(id, dto);
        var etag = "\"" + updated.getId() + "-" + updated.getVersion() + "\"";
        return ResponseEntity.ok().eTag(etag).body(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); // soft-delete portable
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(service.getEstadisticas());
    }

    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> buscarClientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) EstadoCliente estado,
            @ParameterObject Pageable pageable,
            HttpServletRequest req) {

        Page<ClienteDTO> result = service.buscarConFiltros(nombre, apellido, email, estado, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add(HttpHeaders.LINK, buildLinkHeader(req, result));

        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    private String buildLinkHeader(HttpServletRequest req, Page<?> pg){
        var base = req.getRequestURL().toString() + (req.getQueryString()==null? "" : "?"+req.getQueryString());
        String replacePage = base.contains("page=") ? base.replaceAll("([?&])page=\\d+","$1page=%d")
                : (base + (base.contains("?")?"&":"?") + "page=%d");
        List<String> links = new ArrayList<>();
        links.add("<"+ String.format(replacePage, 0) +">; rel=\"first\"");
        links.add("<"+ String.format(replacePage, Math.max(pg.getNumber()-1, 0)) +">; rel=\"prev\"");
        links.add("<"+ String.format(replacePage, pg.getNumber()) +">; rel=\"self\"");
        links.add("<"+ String.format(replacePage, Math.min(pg.getNumber()+1, Math.max(pg.getTotalPages()-1,0))) +">; rel=\"next\"");
        links.add("<"+ String.format(replacePage, Math.max(pg.getTotalPages()-1,0)) +">; rel=\"last\"");
        return String.join(", ", links);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> excelExport() {
        Long userId = SecurityUtils.getCurrentUserId();
        var clientes = service.listAllByUserId(userId);

        log.info("📦 Generando Excel para {} clientes", clientes.size());
        try (InputStream excelStream = ExcelExporter.exportarClientes(clientes)) {
            byte[] contenido = excelStream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(contenido);
        } catch (Exception e) {
            log.error("❌ Error al generar Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClienteService.ResultadoImportacion> importarExcel(@RequestParam("file") MultipartFile file) {
        // 1) Vacío
        if (file == null || file.isEmpty()) {
            var err = new ClienteService.ResultadoImportacion();
            err.setCantidadInsertados(0);
            err.setCantidadErrores(1);
            err.setMensaje("Archivo vacío");
            return ResponseEntity.badRequest().body(err); // 400
        }

        // 2) Tamaño (10MB)
        final long MAX_BYTES = 10L * 1024 * 1024;
        if (file.getSize() > MAX_BYTES) {
            var err = new ClienteService.ResultadoImportacion();
            err.setCantidadInsertados(0);
            err.setCantidadErrores(1);
            err.setMensaje("Archivo demasiado grande (>10MB)");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(err); // 413
        }

        // 3) Tipo/MIME + extensión
        String ct  = Optional.ofNullable(file.getContentType()).orElse("");
        String name= Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();

        boolean okMime = ct.contains("spreadsheet") || ct.contains("excel") || ct.equals("text/csv");
        boolean okExt  = name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".csv");
        if (!(okMime && okExt)) {
            var err = new ClienteService.ResultadoImportacion();
            err.setCantidadInsertados(0);
            err.setCantidadErrores(1);
            err.setMensaje("Formato no permitido. Subí XLSX/XLS o CSV");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(err); // 415
        }

        // 4) Procesar
        try {
            var res = service.importarClientesDesdeExcel(file);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            var err = new ClienteService.ResultadoImportacion();
            err.setCantidadInsertados(0);
            err.setCantidadErrores(1);
            err.setMensaje("Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err); // 400
        }
    }


    @PutMapping("/{id}/estado")
    public ResponseEntity<ClienteDTO> actualizarEstado(@PathVariable Long id, @RequestParam EstadoCliente nuevoEstado) {
        return ResponseEntity.ok(service.actualizarEstado(id, nuevoEstado));
    }
}
