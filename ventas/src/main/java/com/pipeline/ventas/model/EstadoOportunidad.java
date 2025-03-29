package com.pipeline.ventas.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoOportunidad {
    NUEVO, EN_PROCESO, GANADO, PERDIDO;

    @JsonCreator
    public static EstadoOportunidad fromString(String value) {
        return EstadoOportunidad.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
