package com.microclientes.cliente.util;

import com.microclientes.cliente.dto.ClienteDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelExporter {

    public static InputStream exportarClientes(List<ClienteDTO> clientes) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // Estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID", "Nombre", "Apellido", "Email", "Estado", "Fecha Registro"};

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar los datos
            int rowIdx = 1;
            for (ClienteDTO cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cliente.getId() != null ? cliente.getId() : 0);
                row.createCell(1).setCellValue(cliente.getNombre());
                row.createCell(2).setCellValue(cliente.getApellido());
                row.createCell(3).setCellValue(cliente.getEmail());
                row.createCell(4).setCellValue(
                        cliente.getEstado() != null ? cliente.getEstado().name() : "SIN_ESTADO"
                );
                row.createCell(5).setCellValue(
                        cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : ""
                );
            }


            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }
}
