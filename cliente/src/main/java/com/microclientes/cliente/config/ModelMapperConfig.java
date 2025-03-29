package com.microclientes.cliente.config;

import com.microclientes.cliente.model.EstadoCliente;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 💡 Configuración general
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // ✅ Converter personalizado para enums
        Converter<String, EstadoCliente> toEstadoCliente = new Converter<String, EstadoCliente>() {
            @Override
            public EstadoCliente convert(MappingContext<String, EstadoCliente> context) {
                try {
                    return EstadoCliente.valueOf(context.getSource().toUpperCase());
                } catch (IllegalArgumentException | NullPointerException e) {
                    return null;
                }
            }
        };

        modelMapper.createTypeMap(String.class, EstadoCliente.class).setConverter(toEstadoCliente);

        return modelMapper;
    }
}
