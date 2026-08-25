package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.ActualizarUsuarioRequest;
import com.edudev.pedidos_api.dto.CambiarPasswordRequest;
import com.edudev.pedidos_api.dto.CrearUsuarioRequest;
import com.edudev.pedidos_api.dto.UsuarioDTO;
import com.edudev.pedidos_api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody CrearUsuarioRequest request,
            Authentication authentication
    ) {
        try {
            Long adminId = (Long) authentication.getPrincipal();
            UsuarioDTO dto = usuarioService.crear(request, adminId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarUsuarioRequest request,
            Authentication authentication
    ) {
        try {
            Long adminId = (Long) authentication.getPrincipal();
            UsuarioDTO dto = usuarioService.actualizar(id, request, adminId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(
            @PathVariable Long id,
            @RequestBody CambiarPasswordRequest request
    ) {
        try {
            usuarioService.cambiarPassword(id, request);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Long id) {
        try {
            UsuarioDTO dto = usuarioService.activar(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            Long adminId = (Long) authentication.getPrincipal();
            UsuarioDTO dto = usuarioService.desactivar(id, adminId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
