package com.pipeline.ventas.dto;

import com.pipeline.ventas.model.EstadoOportunidad;
import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OportunidadVentaDTO {

    private Long id;

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre del cliente debe tener entre 2 y 100 caracteres")
    private String cliente;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String descripcion;

    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor que 0")
    private Double monto;

    @NotNull(message = "El estado no puede ser nulo")
    private EstadoOportunidad estado;

    @FutureOrPresent(message = "La fecha de cierre estimada debe ser en el futuro o hoy")
    private LocalDate fechaCierreEstimada;
}
