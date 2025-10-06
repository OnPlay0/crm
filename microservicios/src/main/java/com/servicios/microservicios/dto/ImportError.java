package com.servicios.microservicios.dto;

public class ImportError {
    private final int row;         // nro de fila (1-based para incluir header)
    private final String message;  // motivo del error
    public ImportError(int row, String message){ this.row = row; this.message = message; }
    public int getRow(){ return row; }
    public String getMessage(){ return message; }
}
