package com.servicios.microservicios.dto;

import java.util.List;

public class ImportResult {
    private final int inserted;
    private final int updated;
    private final List<ImportError> errors;
    public ImportResult(int inserted, int updated, List<ImportError> errors) {
        this.inserted = inserted; this.updated = updated; this.errors = errors;
    }
    public int getInserted(){ return inserted; }
    public int getUpdated(){ return updated; }
    public List<ImportError> getErrors(){ return errors; }
}
