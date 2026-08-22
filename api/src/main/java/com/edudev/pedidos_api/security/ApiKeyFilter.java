package com.edudev.pedidos_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// ==========================================
// FILTRO API KEY (Fase 6.0)
// ==========================================
// protege únicamente POST /api/pedidos
// consumido por el listener de WhatsApp
// mediante el header X-API-Key
//
// fail-closed: si la clave no está
// configurada o no coincide, se rechaza
// con 401
// ==========================================
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(ApiKeyFilter.class);

    private static final String HEADER_API_KEY = "X-API-Key";

    private static final String METODO_PROTEGIDO = "POST";

    private static final String RUTA_PROTEGIDA = "/api/pedidos";

    private final String apiKey;

    public ApiKeyFilter(
            @Value("${app.security.api-key}") String apiKey
    ) {

        this.apiKey = apiKey == null ? "" : apiKey.trim();

        if (this.apiKey.isEmpty()) {

            log.error(
                    "WHATSAPP_API_KEY no configurada: "
                            + "las creaciones de pedidos del listener "
                            + "serán rechazadas (401)"
            );
        }
    }

    // ==========================================
    // solo filtra POST /api/pedidos
    // el resto de peticiones pasa directo
    // ==========================================

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return !(
                METODO_PROTEGIDO.equalsIgnoreCase(request.getMethod())
                        && RUTA_PROTEGIDA.equals(request.getRequestURI())
        );
    }

    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {

        String claveRecibida = request.getHeader(HEADER_API_KEY);

        if (!claveValida(claveRecibida)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            response.getWriter().write(
                    "{\"error\":\"API Key inválida o ausente\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    // ==========================================
    // comparación en tiempo constante para
    // evitar ataques de timing
    // ==========================================

    private boolean claveValida(String claveRecibida) {

        if (apiKey.isEmpty() || claveRecibida == null) {
            return false;
        }

        return MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                claveRecibida.getBytes(StandardCharsets.UTF_8)
        );
    }
}
