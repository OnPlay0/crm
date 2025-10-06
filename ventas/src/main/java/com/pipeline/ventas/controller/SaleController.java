package com.pipeline.ventas.controller;

import com.pipeline.ventas.dto.*;
import com.pipeline.ventas.service.SaleService;
import com.pipeline.ventas.util.SalesExcel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService service;
    private final SaleService saleService;


    @PostMapping
    public ResponseEntity<SaleDTO> create(@Valid @RequestBody SaleDTO dto, HttpServletRequest req) {
        var created = service.create(dto);
        return ResponseEntity.created(URI.create(req.getRequestURL() + "/" + created.getId()))
                .body(created);
    }

    @GetMapping("/month")
    public ResponseEntity<Page<SaleDTO>> salesOfMonth(@RequestParam int year,
                                                      @RequestParam int month,
                                                      @ParameterObject Pageable pageable,
                                                      HttpServletRequest req) {
        var page = service.salesOfMonth(year, month, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add(HttpHeaders.LINK, buildLinkHeader(req, page));
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/month/detail")
    public List<FlatItemDTO> monthDetail(@RequestParam int year, @RequestParam int month) {
        return service.monthFlatItems(year, month);
    }

    @GetMapping("/month/summary")
    public MonthlySummaryDTO monthSummary(@RequestParam int year, @RequestParam int month) {
        return service.monthSummary(year, month);
    }


    private String buildLinkHeader(HttpServletRequest req, Page<?> pg) {
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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/total")
    public SalesTotalsDTO totalsAllTime() { return service.totalsAllTime(); }


    @GetMapping("/stats/by-product")
    public List<ProductStatsDTO> byProductAllTime() { return service.statsByProductAllTime(); }


    @GetMapping("/stats/by-customer")
    public List<CustomerStatsDTO> byCustomerAllTime() { return service.statsByCustomerAllTime(); }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam int year, @RequestParam int month) {
        var rows = service.monthFlatItems(year, month);
        try (InputStream in = SalesExcel.export(rows)) {
            byte[] bytes = in.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-"+year+"-"+month+".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje","Archivo vacío"));
        }

        try {

            Map<String,Object> result = saleService.importSalesFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "Error al procesar: " + e.getMessage()));
        }
    }


    private String getStr(Row r, int i){
        var c = r.getCell(i);
        return c==null? null : c.getStringCellValue();
    }


    @GetMapping("/month/top-products")
    public List<TopProductDTO> topProducts(@RequestParam int year,
                                           @RequestParam int month,
                                           @RequestParam(defaultValue = "5") int limit) {
        return service.topProducts(year, month, limit);
    }

    @GetMapping("/month/by-day")
    public List<DailyRevenueDTO> byDay(@RequestParam int year, @RequestParam int month) {
        return service.revenueByDay(year, month);
    }

    @GetMapping("/kpis")
    public KpisDTO kpis(@RequestParam String from, @RequestParam String to) {
        LocalDate start = LocalDate.parse(from);
        LocalDate endExclusive = LocalDate.parse(to);
        return service.kpis(start, endExclusive);
    }

    @GetMapping
    public List<SaleResponseDTO> getAllSales() {
        return service.getAllSales();
    }

    @GetMapping("/recent")
    public List<SaleResponseDTO> getRecentSales() {
        return service.getLastSales(5);
    }




}
