package com.taskflow.taskflow_be.security.config;

import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.config.web.RequestCorrelationFilter;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.security.jwt.JwtAuthFilter;
import com.taskflow.taskflow_be.security.jwt.JwtProperties;
import com.taskflow.taskflow_be.security.jwt.JwtService;
import com.taskflow.taskflow_be.security.user.CustomUserDetailsService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, StorageProperties.class})
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtService jwtService(JwtProperties props) {
        return new JwtService(props);
    }

    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository repo) {
        return new CustomUserDetailsService(repo);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, CustomUserDetailsService uds) {
        return new JwtAuthFilter(jwtService, uds);
    }

    @Bean
    public com.taskflow.taskflow_be.security.filter.AuthRateLimitFilter authRateLimitFilter() {
        return new com.taskflow.taskflow_be.security.filter.AuthRateLimitFilter();
    }

    @Bean
    public com.taskflow.taskflow_be.security.filter.ApiRateLimitFilter apiRateLimitFilter() {
        return new com.taskflow.taskflow_be.security.filter.ApiRateLimitFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            RequestCorrelationFilter requestCorrelationFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.setHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, String.valueOf(request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTR)));
                            response.getWriter().write("""
                                {"success":false,"error":{"code":"UNAUTHORIZED","message":"Authentication required","details":null}}
                            """);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.setHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, String.valueOf(request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTR)));
                            response.getWriter().write("""
                                {"success":false,"error":{"code":"FORBIDDEN","message":"Access denied","details":null}}
                            """);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/users/me/public/*/avatar",
                                "/uploads/avatars/**",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
