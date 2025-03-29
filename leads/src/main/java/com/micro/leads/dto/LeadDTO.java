package com.micro.leads.dto;

import com.micro.leads.model.EstadoLead;
import com.micro.leads.model.FuenteLead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeadDTO {

    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    private String telefono;
    private String empresa;

    @NotNull
    private FuenteLead fuente;

    @NotNull
    private EstadoLead estado;

    private String notas;
}
