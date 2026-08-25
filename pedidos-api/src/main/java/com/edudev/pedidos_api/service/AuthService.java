package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.LoginRequest;
import com.edudev.pedidos_api.dto.LoginResponse;
import com.edudev.pedidos_api.entity.Rol;
import com.edudev.pedidos_api.entity.Usuario;
import com.edudev.pedidos_api.repository.UsuarioRepository;
import com.edudev.pedidos_api.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostConstruct
    @Transactional
    public void inicializarAdmin() {
        if (!usuarioRepository.existsByUsuario("admin")) {
            usuarioRepository.save(Usuario.builder()
                    .nombre("Administrador")
                    .usuario("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
            System.out.println("✅ Usuario admin creado (admin / admin123)");
        }
    }

    public LoginResponse login(LoginRequest request) {
        Optional<Usuario> optional =
                usuarioRepository.findByUsuario(request.getUsuario());

        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        Usuario usuario = optional.get();

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getRol().name()
        );

        return LoginResponse.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .usuario(usuario.getUsuario())
                .rol(usuario.getRol().name())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
