package com.edudev.pedidos_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyFilter apiKeyFilter;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(ApiKeyFilter apiKeyFilter, JwtAuthFilter jwtAuthFilter) {
        this.apiKeyFilter = apiKeyFilter;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                .authorizeHttpRequests(auth -> auth
                        // públicos
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/whatsapp/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()

                        // ADMIN
                        .requestMatchers("/api/configuracion/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // dashboard: ADMIN y VENTAS
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "VENTAS")

                        // exportar excel: ADMIN y VENTAS
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/exportar-excel")
                                .hasAnyRole("ADMIN", "VENTAS")

                        // pedidos lectura: todos autenticados
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").authenticated()

                        // pedidos escritura (estado, cancelar, editar): todos autenticados
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").authenticated()

                        // impresión: todos autenticados
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        apiKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();

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
