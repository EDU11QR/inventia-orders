package com.edudev.pedidos_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// ==========================================
// CONFIGURACIÓN DE SEGURIDAD BASE (Fase 6.0)
// ==========================================
// - CSRF desactivado: API REST stateless
// - CORS limitado al origen del dashboard
// - Sesión stateless (preparado para JWT)
// - Todos los endpoints permitAll de forma
//   TRANSITORIA; se endurecerá al implementar
//   login/usuarios (Fase 6.2)
// - ApiKeyFilter protege POST /api/pedidos
// - HomeController sigue público para servir
//   el SPA estático
// ==========================================
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyFilter apiKeyFilter;

    public SecurityConfig(ApiKeyFilter apiKeyFilter) {

        this.apiKeyFilter = apiKeyFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )
                .addFilterBefore(
                        apiKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ==========================================
    // CORS centralizado: solo el origen del
    // panel administrativo en desarrollo.
    // Content-Disposition expuesto para las
    // descargas de PDF y Excel
    // ==========================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion =
                new CorsConfiguration();

        configuracion.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "OPTIONS")
        );

        configuracion.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "X-API-Key")
        );

        configuracion.setExposedHeaders(
                List.of("Content-Disposition")
        );

        configuracion.setAllowCredentials(false);

        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration("/**", configuracion);

        return fuente;
    }
}
