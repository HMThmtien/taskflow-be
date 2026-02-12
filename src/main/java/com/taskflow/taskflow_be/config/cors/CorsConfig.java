package com.taskflow.taskflow_be.config.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource; // <-- nếu lỗi import, đọc note dưới
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173"));
        c.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        // IMPORTANT: Nếu bạn dùng spring-webmvc (Spring Web), class đúng là:
        // org.springframework.web.cors.UrlBasedCorsConfigurationSource
        // Nếu IDE báo sai, hãy đổi import đúng theo dòng dưới:
        // import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);

        return new CorsFilter(source);
    }
}
