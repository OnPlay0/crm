package com.pipeline.ventas.util;

import com.pipeline.ventas.dto.FlatItemDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.List;

public class SalesExcel {
    public static InputStream export(List<FlatItemDTO> rows) throws IOException {
        try (var wb = new XSSFWorkbook()) {
            var sh = wb.createSheet("sales");
            int r = 0;
            String[] head = {"Date","SaleID","CustomerID","SKU","Product","Description","Qty","UnitPrice","Subtotal"};
            var h = sh.createRow(r++);
            for (int i=0;i<head.length;i++) h.createCell(i).setCellValue(head[i]);

            for (var x : rows) {
                Row row = sh.createRow(r++);
                int c=0;
                row.createCell(c++).setCellValue(x.getDate()==null? "": x.getDate().toString());
                row.createCell(c++).setCellValue(x.getSaleId()==null? 0: x.getSaleId());
                row.createCell(c++).setCellValue(x.getCustomerId()==null? 0: x.getCustomerId());
                row.createCell(c++).setCellValue(x.getSku()==null? "": x.getSku());
                row.createCell(c++).setCellValue(x.getProduct()==null? "": x.getProduct());
                row.createCell(c++).setCellValue(x.getDescription()==null? "": x.getDescription());
                row.createCell(c++).setCellValue(x.getQuantity()==null? 0: x.getQuantity());
                row.createCell(c++).setCellValue(x.getUnitPrice()==null? 0: x.getUnitPrice());
                row.createCell(c++).setCellValue(x.getSubtotal()==null? 0: x.getSubtotal());
            }
            for (int i=0;i<head.length;i++) sh.autoSizeColumn(i);
            var out = new ByteArrayOutputStream();
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
