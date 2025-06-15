package com.microclientes.cliente.mapper;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.model.Cliente;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {
    ClienteDTO toDto(Cliente entity);
    Cliente toEntity(ClienteDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ClienteDTO dto, @MappingTarget Cliente entity);
}