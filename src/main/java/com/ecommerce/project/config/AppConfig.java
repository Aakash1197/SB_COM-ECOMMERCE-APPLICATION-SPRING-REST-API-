package com.ecommerce.project.config;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AppConfig {
    private ModelMapper modelMapper;
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
