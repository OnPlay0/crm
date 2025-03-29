package com.micro.leads.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoLead {
    NUEVO, EN_PROCESO, CONTACTADO, CALIFICADO, DESCARTADO;

    @JsonCreator
    public static EstadoLead fromString(String value) {
        return EstadoLead.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
