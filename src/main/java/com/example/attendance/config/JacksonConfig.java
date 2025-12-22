package com.example.attendance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Automatically find and register modules (like JavaTimeModule) available on the classpath
        // This avoids the compilation error caused by explicitly importing the missing package
        mapper.findAndRegisterModules();
        return mapper;
    }
}