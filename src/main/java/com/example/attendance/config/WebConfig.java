package com.example.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Apply CORS settings to all endpoints (/**)
        registry.addMapping("/**")
                // Allow the frontend URL (adjust port 3000 if your Next.js app runs elsewhere)
                .allowedOrigins("http://localhost:3000")
                // Allow specific HTTP methods
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Allow all headers
                .allowedHeaders("*")
                // Allow credentials (cookies, authorization headers)
                .allowCredentials(true);
    }
}