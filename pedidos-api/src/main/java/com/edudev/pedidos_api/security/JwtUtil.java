package com.edudev.pedidos_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long DURACION_MS = 24 * 60 * 60 * 1000L;

    private final SecretKey signingKey;

    public JwtUtil(
            @Value("${app.security.jwt-secret}") String secret
    ) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres. "
                            + "Exportar antes de arrancar: export JWT_SECRET=..."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generarToken(Long id, String usuario, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + DURACION_MS);

        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("usuario", usuario)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
