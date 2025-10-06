package com.microclientes.cliente.model;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un cliente")
public enum EstadoCliente {
    @Schema(description = "Cliente potencial aún no confirmado")
    PROSPECTO,

    @Schema(description = "Cliente activo en el sistema")
    ACTIVO,

    @Schema(description = "Cliente inactivo temporalmente")
    INACTIVO,

    @Schema(description = "Relación comercial cerrada")
    CERRADO
}


