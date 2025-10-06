package com.pipeline.ventas.config;


import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pipeline.ventas.dto.SaleItemDTO;
import com.pipeline.ventas.model.SaleItem;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Map snapshot fields -> DTO visible fields
        mapper.typeMap(SaleItem.class, SaleItemDTO.class)
                .addMappings(m -> {
                    m.map(SaleItem::getSkuSnapshot, SaleItemDTO::setSku);
                    m.map(SaleItem::getNameSnapshot, SaleItemDTO::setName);
                    m.map(SaleItem::getDescriptionSnapshot, SaleItemDTO::setDescription);
                    m.map(src -> Boolean.TRUE.equals(src.getService()), SaleItemDTO::setService);
                });

        return mapper;
    }
}

