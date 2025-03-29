package com.servicios.microservicios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria.")
    private String descripcion;

    @NotNull(message = "El precio no puede ser nulo.")
    @Positive(message = "El precio debe ser mayor a cero.")
    private Double precio;
}
