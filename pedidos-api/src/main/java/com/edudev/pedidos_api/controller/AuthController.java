package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.LoginRequest;
import com.edudev.pedidos_api.dto.LoginResponse;
import com.edudev.pedidos_api.entity.Usuario;
import com.edudev.pedidos_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "No autenticado"));
        }

        Long id = (Long) authentication.getPrincipal();
        Optional<Usuario> optional = authService.obtenerPorId(id);

        if (optional.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = optional.get();

        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "usuario", usuario.getUsuario(),
                "rol", usuario.getRol().name()
        ));
    }
}
