package com.microclientes.cliente.service;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.exception.ResourceNotFoundException;
import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.model.EstadoCliente;
import com.microclientes.cliente.repository.ClienteRepository;
import com.microclientes.cliente.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository repo;
    private final ModelMapper modelMapper;

    @PostConstruct
    void configureMapper() {
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
       
        modelMapper.typeMap(ClienteDTO.class, Cliente.class).addMappings(m -> {
            m.skip(Cliente::setId);
            m.skip(Cliente::setUserId);
            m.skip(Cliente::setCreatedAt);
            m.skip(Cliente::setUpdatedAt);
            m.skip(Cliente::setCreatedBy);
            m.skip(Cliente::setUpdatedBy);
            m.skip(Cliente::setDeletedAt);
            m.skip(Cliente::setVersion);
            // fechaRegistro la setea el servicio en create si viene null
        });
    }

    private Long uid() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new IllegalStateException("No hay X-User-Id en el request");
        return userId;
    }

    private String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase();
    }

    // ---------- CRUD & Query ----------

    public Page<ClienteDTO> list(Pageable page) {
        Long userId = uid();
        var pg = repo.findByUserIdAndDeletedAtIsNull(userId, page);
        return pg.map(c -> modelMapper.map(c, ClienteDTO.class));
    }

    public ClienteDTO get(Long id) {
        var c = repo.findByIdAndUserIdAndDeletedAtIsNull(id, uid())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        return modelMapper.map(c, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO create(ClienteDTO dto) {
        Long userId = uid();
        dto.setEmail(normalizeEmail(dto.getEmail()));

        if (dto.getEmail() != null && repo.existsByUserIdAndEmailIgnoreCaseAndDeletedAtIsNull(userId, dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        }

        Cliente c = modelMapper.map(dto, Cliente.class);
        c.setUserId(userId);
        if (c.getFechaRegistro() == null) c.setFechaRegistro(LocalDateTime.now());
        if (c.getEstado() == null) c.setEstado(EstadoCliente.PROSPECTO);

        return modelMapper.map(repo.save(c), ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO update(Long id, ClienteDTO dto) {
        Long userId = uid();
        Cliente existing = repo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));

        String newEmail = normalizeEmail(dto.getEmail());
        if (newEmail != null && !newEmail.equalsIgnoreCase(existing.getEmail())
                && repo.existsByUserIdAndEmailIgnoreCaseAndDeletedAtIsNull(userId, newEmail)) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        }

        modelMapper.map(dto, existing);
        existing.setEmail(newEmail); // asegurar normalización
        // userId/id/createdAt/version no se tocan por el mapper config

        return modelMapper.map(repo.save(existing), ClienteDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = uid();
        // Soft delete portable + libera la constraint de email
        Cliente c = repo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));

        // marcar borrado
        c.setDeletedAt(LocalDateTime.now());

        if (c.getEmail() != null) {
            final int MAX_LEN = 120; // coincide con @Column(length=120)
            String base = c.getEmail();
            String suf  = "#deleted#" + c.getId();
            int maxBase = Math.max(0, MAX_LEN - suf.length());
            if (base.length() > maxBase) {
                base = base.substring(0, maxBase);
            }
            c.setEmail(base + suf);
        }

        repo.save(c);
    }

    public Long count() {
        return repo.countByUserIdAndDeletedAtIsNull(uid());
    }

    public Map<String, Object> getEstadisticas() {
        Long userId = uid();
        var clientes = repo.findByUserIdAndDeletedAtIsNull(userId);

        long total = clientes.size();
        Map<String, Long> porEstado = clientes.stream()
                .collect(Collectors.groupingBy(c -> c.getEstado().name(), Collectors.counting()));

        var ultimos = clientes.stream()
                .sorted(Comparator.comparing(Cliente::getFechaRegistro, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .map(c -> Map.of("id", c.getId(), "nombre", c.getNombre(), "estado", c.getEstado().name()))
                .toList();

        return Map.of("total", total, "porEstado", porEstado, "ultimosClientes", ultimos);
    }

    public Page<ClienteDTO> buscarConFiltros(String nombre, String apellido, String email, EstadoCliente estado, Pageable pageable) {
        Long userId = uid();
        String n = nombre == null ? null : nombre.trim();
        String a = apellido == null ? null : apellido.trim();
        String e = email == null ? null : email.trim().toLowerCase();
        var page = repo.buscarPorFiltros(userId, n, a, e, estado, pageable);
        return page.map(c -> modelMapper.map(c, ClienteDTO.class));
    }

    public List<ClienteDTO> listAllByUserId(Long userId) {
        return repo.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(c -> modelMapper.map(c, ClienteDTO.class))
                .toList();
    }

    @Transactional
    public ClienteDTO actualizarEstado(Long id, EstadoCliente nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Estado requerido");
        }
        Long userId = uid();
        Cliente c = repo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));


        c.setEstado(nuevoEstado);
        c = repo.save(c);
        return modelMapper.map(c, ClienteDTO.class);
    }


    // ---------- Import desde Excel (alineado a tu entidad) ----------

    @Data
    public static class ResultadoImportacion {
        private int cantidadInsertados;
        private int cantidadErrores;
        private int cantidadActualizados; // si hacés upsert
        private String mensaje;
        private List<DetalleError> errores = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class DetalleError {
        private int fila;      // Nº de fila del Excel (1-based o el rowNum que uses)
        private String email;  // el email que falló (si lo tenés parseado)
        private String motivo; // ej: "Email duplicado" / "Email inválido" / etc.
    }


    public ResultadoImportacion importarClientesDesdeExcel(MultipartFile file) throws Exception {
        Long userId = uid();
        int ok = 0, bad = 0, upd = 0;
        var res = new ResultadoImportacion();

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1)
                throw new IllegalArgumentException("El archivo Excel está vacío");

            Map<String,Integer> col = new HashMap<>();
            for (Cell c : sheet.getRow(0)) col.put(c.getStringCellValue().trim().toLowerCase(), c.getColumnIndex());

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String email = null; // para reportar en errores
                try {
                    String nombre   = getString(row, col.get("nombre"));
                    String apellido = getString(row, col.get("apellido"));
                    email           = normalizeEmail(getString(row, col.get("email")));
                    String telefono = getString(row, col.get("telefono"));
                    String direccion= getString(row, col.get("direccion"));
                    String estado   = getString(row, col.get("estado"));

                    if (nombre == null || nombre.isBlank())   throw new IllegalArgumentException("Falta nombre");
                    if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("Falta apellido");
                    if (email == null || !email.contains("@"))  throw new IllegalArgumentException("Email inválido");

                    if (repo.existsByUserIdAndEmailIgnoreCaseAndDeletedAtIsNull(userId, email))
                        throw new IllegalArgumentException("Email duplicado");

                    var c = new Cliente();
                    c.setUserId(userId);
                    c.setNombre(nombre);
                    c.setApellido(apellido);
                    c.setEmail(email);
                    c.setTelefono(telefono);
                    c.setDireccion(direccion);
                    c.setFechaRegistro(LocalDateTime.now());
                    try { if (estado != null) c.setEstado(EstadoCliente.valueOf(estado.trim().toUpperCase())); }
                    catch (Exception ignored) { c.setEstado(EstadoCliente.PROSPECTO); }

                    repo.save(c);
                    ok++;
                } catch (Exception ex) {
                    bad++;
                    log.warn("Fila {} omitida: {}", r, ex.getMessage());
                    res.getErrores().add(new DetalleError(r, email, ex.getMessage()));
                }
            }
        }

        res.setCantidadInsertados(ok);
        res.setCantidadErrores(bad);
        res.setCantidadActualizados(upd); // si no hacés upsert, dejá 0
        res.setMensaje(String.format("Se cargaron %d de %d filas.", ok, ok + bad));
        return res;
    }


    private String getString(Row row, Integer idx) {
        if (idx == null) return null;
        var cell = row.getCell(idx);
        return cell == null ? null : cell.toString().trim();
    }

}
