package com.microclientes.cliente.dto;

import com.microclientes.cliente.model.EstadoCliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Schema(description = "DTO para creación y consulta de clientes")
public class ClienteDTO {

    @Schema(description = "ID del cliente", example = "1")
    private Long id;

    @Schema(description = "Nombre del cliente", example = "Ana")
    @NotBlank @Size(min = 2, max = 50)
    private String nombre;

    @Schema(description = "Apellido del cliente", example = "García")
    @NotBlank @Size(min = 2, max = 50)
    private String apellido;

    @Schema(description = "Email del cliente", example = "ana@mail.com")
    @Email @NotBlank
    private String email;

    @Schema(description = "Teléfono del cliente (10 dígitos)", example = "1155544433")
    @Pattern(regexp = "\\d{10}")
    private String telefono;

    @Schema(description = "Dirección del cliente", example = "Av. Siempre Viva 123")
    private String direccion;

    @Schema(description = "Estado actual del cliente", example = "ACTIVO")
    @NotNull
    private EstadoCliente estado;

    private Long version;

    private LocalDateTime fechaRegistro;
}
