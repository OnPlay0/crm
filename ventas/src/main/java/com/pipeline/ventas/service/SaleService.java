package com.pipeline.ventas.service;

import com.pipeline.ventas.dto.*;
import com.pipeline.ventas.model.*;
import com.pipeline.ventas.repositories.*;
import com.pipeline.ventas.service.client.ProductsClient;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepo;
    private final SaleItemRepository itemRepo;
    private final ModelMapper mapper;

    // Optional: only if you define a real bean
    @Autowired(required = false)
    private ProductsClient productsClient;

    private Long userId() {
        String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(uid);
    }

    @Transactional
    public SaleDTO create(SaleDTO dto) {
        Long uid = userId();
        if (dto.getItems() == null || dto.getItems().isEmpty())
            throw new IllegalArgumentException("La venta debe tener al menos un ítem.");

        Sale s = new Sale();
        s.setUserId(uid);
        s.setDate(Optional.ofNullable(dto.getDate()).orElse(LocalDate.now()));
        s.setCustomerId(dto.getCustomerId());
        s.setNotes(dto.getNotes());

        double total = 0d;

        for (SaleItemDTO it : dto.getItems()) {
            int qty = Optional.ofNullable(it.getQuantity()).orElse(0);
            double unit = Optional.ofNullable(it.getUnitPrice()).orElse(0d);
            if (qty <= 0) throw new IllegalArgumentException("quantity debe ser > 0");
            if (unit <= 0) throw new IllegalArgumentException("unitPrice debe ser > 0");

            // 🚀 Venta rápida (sin productId) → creo en catálogo
            if (it.getProductId() == null) {
                CatalogItemDTO nuevo = CatalogItemDTO.builder()
                        .type(Boolean.TRUE.equals(it.getService()) ? "SERVICE" : "PRODUCT")
                        .sku(it.getSku() != null ? it.getSku() : "VR-" + System.currentTimeMillis())
                        .name(it.getName() != null ? it.getName() : "Venta rápida")
                        .description(it.getDescription())
                        .price(it.getUnitPrice())
                        .stock(Boolean.TRUE.equals(it.getService()) ? null : it.getQuantity())
                        .active(true)
                        .category("VENTA_RAPIDA")
                        .build();

                CatalogItemDTO creado = catalogClient.create(nuevo);
                it.setProductId(creado.getId()); // guardamos el id del nuevo item
            }

            // Snapshot
            String sku, name, desc; boolean isService;
            if (productsClient != null) {
                var p = productsClient.getById(it.getProductId());
                sku = p.getSku();
                name = p.getName();
                desc = Optional.ofNullable(p.getDescription()).orElse("");
                isService = p.isService();
            } else {
                sku = Optional.ofNullable(it.getSku()).orElse("N/A");
                name = Optional.ofNullable(it.getName()).orElse("Venta rápida");
                desc = Optional.ofNullable(it.getDescription()).orElse("");
                isService = Boolean.TRUE.equals(it.getService());
            }

            double subtotal = unit * qty;

            var si = SaleItem.builder()
                    .sale(s)
                    .productId(it.getProductId())
                    .skuSnapshot(sku)
                    .nameSnapshot(name)
                    .descriptionSnapshot(desc)
                    .unitPrice(unit)
                    .quantity(qty)
                    .subtotal(subtotal)
                    .service(isService)
                    .build();

            s.getItems().add(si);
            total += subtotal;
        }

        s.setTotal(total);
        var saved = saleRepo.save(s);

        // stock OUT
        try {
            if (productsClient != null) {
                for (SaleItem si : saved.getItems()) {
                    if (!Boolean.TRUE.equals(si.getService())) {
                        productsClient.stockOut(si.getProductId(), si.getQuantity(), "SALE#" + saved.getId());
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descontar stock: " + e.getMessage(), e);
        }

        return mapper.map(saved, SaleDTO.class);
    }



    @Transactional(readOnly = true)
    public Page<SaleDTO> salesOfMonth(int year, int month, Pageable pageable) {
        Long uid = userId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        return saleRepo.findByDateRange(uid, start, end, pageable)
                .map(s -> mapper.map(s, SaleDTO.class));
    }

    @Transactional(readOnly = true)
    public List<FlatItemDTO> monthFlatItems(int year, int month) {
        Long uid = userId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        return itemRepo.itemsByDateRange(uid, start, end);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryDTO monthSummary(int year, int month) {
        var items = monthFlatItems(year, month);
        long sales = items.stream().map(FlatItemDTO::getSaleId).distinct().count();
        long itemCount = items.stream().mapToLong(i -> Optional.ofNullable(i.getQuantity()).orElse(0)).sum();
        double revenue = items.stream().mapToDouble(i -> Optional.ofNullable(i.getSubtotal()).orElse(0.0)).sum();
        return MonthlySummaryDTO.builder()
                .sales(sales)
                .items(itemCount)
                .revenue(revenue)
                .build();
    }

    @Transactional
    public void delete(Long saleId) {
        Long uid = userId();
        int updated = saleRepo.softDelete(uid, saleId);
        if (updated == 0) throw new IllegalArgumentException("Venta no encontrada o ya eliminada");
    }

    @Transactional(readOnly = true)
    public SalesTotalsDTO totalsAllTime() {
        Long uid = userId();
        var rows = itemRepo.itemsByDateRange(uid, LocalDate.of(1970,1,1), LocalDate.now().plusDays(1));
        long sales = rows.stream().map(FlatItemDTO::getSaleId).distinct().count();
        long items = rows.stream().mapToLong(r -> r.getQuantity()==null?0:r.getQuantity()).sum();
        double revenue = rows.stream().mapToDouble(r -> r.getSubtotal()==null?0:r.getSubtotal()).sum();
        double avgTicket = sales==0 ? 0.0 : revenue / sales;
        return SalesTotalsDTO.builder().sales(sales).items(items).revenue(revenue).avgTicket(avgTicket).build();
    }

    @Transactional(readOnly = true)
    public List<ProductStatsDTO> statsByProductAllTime() {
        Long uid = userId();
        var rows = itemRepo.itemsByDateRange(
                uid,
                LocalDate.of(1970, 1, 1),
                LocalDate.now().plusDays(1)
        );

        // record local (requiere Java 16+; estás en 17, ok)
        record Key(String sku, String name) {}

        Map<Key, List<FlatItemDTO>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        (FlatItemDTO r) -> new Key(
                                r.getSku() == null ? "" : r.getSku(),
                                r.getProduct() == null ? "" : r.getProduct()
                        )
                ));

        return grouped.entrySet().stream()
                .map(e -> {
                    long qty = e.getValue().stream()
                            .mapToLong(r -> r.getQuantity() == null ? 0 : r.getQuantity())
                            .sum();

                    double tot = e.getValue().stream()
                            .mapToDouble(r -> r.getSubtotal() == null ? 0 : r.getSubtotal())
                            .sum();

                    return ProductStatsDTO.builder()
                            .sku(e.getKey().sku())
                            .name(e.getKey().name())
                            .quantity(qty)
                            .total(tot)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<CustomerStatsDTO> statsByCustomerAllTime() {
        Long uid = userId();
        var rows = itemRepo.itemsByDateRange(uid, LocalDate.of(1970,1,1), LocalDate.now().plusDays(1));
        var grouped = rows.stream().collect(Collectors.groupingBy(FlatItemDTO::getCustomerId));
        return grouped.entrySet().stream().map(e -> {
                    long sales = e.getValue().stream().map(FlatItemDTO::getSaleId).distinct().count();
                    double total = e.getValue().stream().mapToDouble(r -> r.getSubtotal()==null?0:r.getSubtotal()).sum();
                    return CustomerStatsDTO.builder().customerId(e.getKey()).sales(sales).total(total).build();
                }).sorted((a,b) -> Double.compare(b.getTotal(), a.getTotal()))
                .toList();
    }

    public List<TopProductDTO> topProducts(int year, int month, int limit) {
        Long uid = userId();
        var ym = YearMonth.of(year, month);
        var start = ym.atDay(1);
        var end = ym.plusMonths(1).atDay(1);
        return itemRepo.topProducts(uid, start, end, PageRequest.of(0, limit));
    }

    public List<DailyRevenueDTO> revenueByDay(int year, int month) {
        Long uid = userId();
        var ym = YearMonth.of(year, month);
        var start = ym.atDay(1);
        var end = ym.plusMonths(1).atDay(1);
        return saleRepo.revenueByDay(uid, start, end);
    }

    public KpisDTO kpis(LocalDate from, LocalDate toExclusive) {
        Long uid = userId();
        var a = saleRepo.salesAndRevenue(uid, from, toExclusive);
        long sales = ((Number)a[0]).longValue();
        double revenue = ((Number)a[1]).doubleValue();

        var b = itemRepo.itemsRevenueAndServices(uid, from, toExclusive);
        long items = ((Number)b[0]).longValue();
        double itemsRevenue = ((Number)b[1]).doubleValue();
        double servicesRevenue = ((Number)b[2]).doubleValue();

        return KpisDTO.builder()
                .sales(sales)
                .items(items)
                .revenue(revenue)
                .avgTicket(sales > 0 ? revenue / sales : 0d)
                .pctServices(revenue > 0 ? (servicesRevenue / revenue) * 100.0 : 0d)
                .build();
    }


    public List<SaleResponseDTO> getAllSales() {
        return saleRepo.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }


    @Autowired
    private CatalogClient catalogClient;

    private SaleResponseDTO toResponseDTO(Sale sale) {

        SaleItem firstItem = (sale.getItems() != null && !sale.getItems().isEmpty())
                ? sale.getItems().get(0)
                : null;

        String nombreProd = "Venta rápida";
        String tipoProd = "-";

        if (firstItem != null) {
            try {
                CatalogItemDTO prod = catalogClient.getById(firstItem.getProductId());
                if (prod != null) {
                    // el DTO nuevo usa `name` y `type`
                    nombreProd = prod.getName() != null ? prod.getName() : "Venta rápida";
                    tipoProd = prod.getType() != null ? prod.getType() : "-"; // "PRODUCT" o "SERVICE"
                }
            } catch (Exception e) {
                // fallback a snapshot
                nombreProd = firstItem.getNameSnapshot() != null ? firstItem.getNameSnapshot() : "Venta rápida";
                tipoProd = Boolean.TRUE.equals(firstItem.getService()) ? "SERVICE" : "PRODUCT";
            }
        }

        return SaleResponseDTO.builder()
                .id(sale.getId())
                .date(sale.getDate())
                .description(sale.getNotes())
                .total(sale.getTotal())
                .productName(nombreProd)
                .productType(tipoProd)
                .estado("NUEVO")
                .fechaCierreEstimada(sale.getDate())
                .build();
    }


    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getLastSales(int limit) {
        return saleRepo.findAllByOrderByDateDesc(
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Map<String,Object> importSalesFromExcel(MultipartFile file) throws Exception {
        final long MAX = 10L * 1024 * 1024;
        if (file.getSize() > MAX)
            throw new MaxUploadSizeExceededException(MAX);

        String ct = Optional.ofNullable(file.getContentType()).orElse("");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();

        boolean okMime = ct.contains("spreadsheet") || ct.contains("excel") || ct.equals("text/csv");
        boolean okExt  = name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".csv");
        if (!(okMime && okExt))
            throw new UnsupportedOperationException("Formato no permitido. Subí XLSX/XLS o CSV");

        int insertados = 0, errores = 0;

        try (var wb = new XSSFWorkbook(file.getInputStream())) {
            var sh = wb.getSheetAt(0);
            boolean header = true;

            for (Row row : sh) {
                if (header) { header=false; continue; }
                if (row == null) continue;

                try {
                    var dto = SaleDTO.builder()
                            .date(LocalDate.parse(row.getCell(0).getStringCellValue()))
                            .customerId((long) row.getCell(2).getNumericCellValue())
                            .items(List.of(SaleItemDTO.builder()
                                    .sku(getStr(row,3))
                                    .name(getStr(row,4))
                                    .description(getStr(row,5))
                                    .quantity((int) row.getCell(6).getNumericCellValue())
                                    .unitPrice(row.getCell(7).getNumericCellValue())
                                    .service(false)
                                    .build()))
                            .build();

                    this.create(dto);
                    insertados++;
                } catch (Exception ex) { errores++; }
            }
        }

        return Map.of("insertados", insertados, "errores", errores);
    }

    private String getStr(Row row, int index) {
        return Optional.ofNullable(row.getCell(index))
                .map(Cell::getStringCellValue)
                .orElse("");
    }

}
