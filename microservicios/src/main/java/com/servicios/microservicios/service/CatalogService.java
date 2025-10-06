package com.servicios.microservicios.service;


import com.servicios.microservicios.dto.CatalogItemDTO;
import com.servicios.microservicios.model.*;
import com.servicios.microservicios.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.servicios.microservicios.dto.ImportResult;
import com.servicios.microservicios.dto.ImportError;


import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class CatalogService {
    private final CatalogItemRepository repo;
    private final InventoryMovementRepository movRepo;
    private final ModelMapper mapper;

    private Long uid(){
        String u = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(u);
    }

    public static class ProductSnapshot {
        public final Long id;
        public final String sku;
        public final String name;
        public final String description;
        public final boolean service;
        public ProductSnapshot(Long id, String sku, String name, String description, boolean service) {
            this.id = id; this.sku = sku; this.name = name; this.description = description; this.service = service;
        }
    }


    @Transactional(readOnly = true)
    public ProductSnapshot getById(Long id){
        var item = repo.findByIdAndUserIdAndDeletedAtIsNull(id, uid())
                .orElseThrow(() -> new IllegalArgumentException("Producto/Servicio no encontrado"));
        return new ProductSnapshot(
                item.getId(), item.getSku(), item.getName(), item.getDescription(),
                item.getType() == ItemType.SERVICE
        );
    }

    // CatalogService
    @Transactional
    public CatalogItemDTO update(Long id, CatalogItemDTO dto) {
        var item = repo.findByIdAndUserIdAndDeletedAtIsNull(id, uid())
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(dto.getCategory());
        item.setActive(dto.getActive() == null ? Boolean.TRUE : dto.getActive());
        if (item.getType() == ItemType.PRODUCT) {
            // si querés permitir stock inicial o sku acá
            item.setSku(dto.getSku());
        }
        return mapper.map(repo.save(item), CatalogItemDTO.class);
    }


    @Transactional
    public CatalogItemDTO create(CatalogItemDTO dto){
        Long userId = uid();

        if (repo.existsByUserIdAndSkuIgnoreCaseAndDeletedAtIsNull(userId, dto.getSku()))
            throw new IllegalArgumentException("SKU ya existe");

        if (dto.getType() == null) throw new IllegalArgumentException("type obligatorio (PRODUCT|SERVICE)");
        final ItemType it;
        try { it = ItemType.valueOf(dto.getType().trim().toUpperCase()); }
        catch (Exception e) { throw new IllegalArgumentException("type inválido: " + dto.getType()); }

        var item = mapper.map(dto, CatalogItem.class);
        item.setId(null);
        item.setUserId(userId);
        item.setType(it);
        if (it == ItemType.SERVICE) item.setStock(null);

        var saved = repo.save(item);

        if (saved.getType()==ItemType.PRODUCT && dto.getStock()!=null && dto.getStock()>0){
            movRepo.save(InventoryMovement.builder()
                    .userId(userId).item(saved).type(MovementType.IN).quantity(dto.getStock())
                    .reason("INITIAL").build());
            saved.setStock(dto.getStock());
        }
        return mapper.map(saved, CatalogItemDTO.class);
    }


    public Page<CatalogItemDTO> search(String q, String type, String category, Boolean active, Pageable pg){
        ItemType t = (type==null? null : ItemType.valueOf(type.toUpperCase()));
        return repo.search(uid(), q, t, category, active, pg).map(e -> mapper.map(e, CatalogItemDTO.class));
    }

    @Transactional
    public void delete(Long id){
        if (repo.softDelete(uid(), id)==0) throw new IllegalArgumentException("No se pudo borrar");
    }

    @Transactional
    public void stockOut(Long itemId, int qty, String reason){
        var item = repo.findByIdAndUserIdAndDeletedAtIsNull(itemId, uid())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (item.getType()==ItemType.SERVICE) return;

        if (qty<=0) throw new IllegalArgumentException("qty debe ser > 0");
        int current = item.getStock()==null ? 0 : item.getStock();
        if (current < qty) throw new IllegalStateException("Stock insuficiente");

        item.setStock(current - qty);
        movRepo.save(InventoryMovement.builder()
                .userId(uid()).item(item).type(MovementType.OUT).quantity(-qty) // << positivo
                .reason(reason).build());
    }


    @Transactional(readOnly = true)
    public void exportServiciosExcel(OutputStream os) throws IOException {
        Long userId = uid();
        List<CatalogItem> services = repo
                .findAllByUserIdAndTypeAndDeletedAtIsNullOrderByIdDesc(userId, ItemType.SERVICE);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Servicios");
            int r = 0;
            String[] cols = {"id", "name", "description", "price", "category", "active"}; // << price

            Row header = sh.createRow(r++);
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            for (CatalogItem it : services) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(it.getId());
                row.createCell(1).setCellValue(nz(it.getName()));
                row.createCell(2).setCellValue(nz(it.getDescription()));
                row.createCell(3).setCellValue(it.getPrice() == null ? 0 : it.getPrice().doubleValue()); // << price
                row.createCell(4).setCellValue(nz(it.getCategory()));
                row.createCell(5).setCellValue(Boolean.TRUE.equals(it.getActive()));
            }
            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);
            wb.write(os);
            os.flush();
        }
    }


    private String nz(String s) { return s == null ? "" : s; }

    @Transactional
    public ImportResult importServiciosExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Archivo vacío");

        Long userId = uid();
        int inserted = 0, updated = 0;
        List<ImportError> errors = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sh = wb.getSheetAt(0);
            if (sh == null) throw new IllegalArgumentException("La planilla no tiene hojas");

            for (int i = 1; i <= sh.getLastRowNum(); i++) {
                Row row = sh.getRow(i);
                if (row == null) continue;

                try {
                    Long id          = getLong(row, 0);
                    String name      = getString(row, 1);
                    String desc      = getString(row, 2);
                    BigDecimal price = getBigDecimal(row, 3);
                    String category  = getString(row, 4);
                    Boolean active   = getBoolean(row, 5);

                    if (name == null || name.isBlank()) throw new IllegalArgumentException("name obligatorio");
                    if (price == null) price = BigDecimal.ZERO;

                    CatalogItem entity;
                    if (id != null) {
                        entity = repo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                                .orElseThrow(() -> new IllegalArgumentException("ID no pertenece al usuario o está borrado"));
                        if (entity.getType() != ItemType.SERVICE)
                            throw new IllegalStateException("El id " + id + " no es SERVICE");
                        entity.setName(name);
                        entity.setDescription(desc);
                        entity.setPrice(price.doubleValue());   // << price
                        entity.setCategory(category);
                        entity.setActive(active != null ? active : Boolean.TRUE);
                        updated++;
                    } else {
                        entity = new CatalogItem();
                        entity.setUserId(userId);
                        entity.setType(ItemType.SERVICE);
                        entity.setName(name);
                        entity.setDescription(desc);
                        entity.setPrice(price.doubleValue());   // << price
                        entity.setCategory(category);
                        entity.setActive(active != null ? active : Boolean.TRUE);
                        inserted++;
                    }

                    repo.save(entity);
                } catch (Exception ex) {
                    errors.add(new ImportError(i + 1, ex.getMessage()));
                }
            }
        }
        return new ImportResult(inserted, updated, errors);
    }

    private Long getLong(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                String s = cell.getStringCellValue().trim();
                yield s.isEmpty() ? null : Long.parseLong(s);
            }
            default -> null;
        };
    }

    private String getString(Row row, int idx) {
        var cell = row.getCell(idx);
        return (cell == null) ? null : cell.toString().trim();
    }

    private java.math.BigDecimal getBigDecimal(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> java.math.BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String s = cell.getStringCellValue().trim().replace(",", ".");
                yield s.isEmpty() ? null : new java.math.BigDecimal(s);
            }
            default -> null;
        };
    }

    private Boolean getBoolean(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> {
                String s = cell.getStringCellValue().trim().toLowerCase();
                if (s.isEmpty()) yield null;
                yield s.equals("true") || s.equals("1") || s.equals("si") || s.equals("sí");
            }
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> null;
        };
    }
}
