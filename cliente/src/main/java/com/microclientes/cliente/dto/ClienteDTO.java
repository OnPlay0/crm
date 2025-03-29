package com.microclientes.cliente.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.microclientes.cliente.model.EstadoCliente;

@Getter
@Setter
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @Email(message = "El correo electrónico debe ser válido")
    @NotBlank(message = "El correo no puede estar vacío")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "El teléfono debe contener 10 dígitos")
    private String telefono;

    private String direccion;

    @NotNull(message = "El estado es obligatorio")
    private EstadoCliente estado;

}
