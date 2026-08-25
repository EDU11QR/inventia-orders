package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.ActualizarUsuarioRequest;
import com.edudev.pedidos_api.dto.CambiarPasswordRequest;
import com.edudev.pedidos_api.dto.CrearUsuarioRequest;
import com.edudev.pedidos_api.dto.UsuarioDTO;
import com.edudev.pedidos_api.entity.Rol;
import com.edudev.pedidos_api.entity.Usuario;
import com.edudev.pedidos_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioDTO::fromEntity)
                .toList();
    }

    @Transactional
    public UsuarioDTO crear(CrearUsuarioRequest request, Long adminId) {

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.getUsuario() == null || request.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        if (request.getRol() == null || request.getRol().trim().isEmpty()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        String usuarioLower = request.getUsuario().trim().toLowerCase();

        if (usuarioRepository.existsByUsuario(usuarioLower)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Rol rol;
        try {
            rol = Rol.valueOf(request.getRol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + request.getRol());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .usuario(usuarioLower)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);

        return UsuarioDTO.fromEntity(usuario);
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, ActualizarUsuarioRequest request, Long adminId) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.getRol() == null || request.getRol().trim().isEmpty()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        Rol nuevoRol;
        try {
            nuevoRol = Rol.valueOf(request.getRol().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido: " + request.getRol());
        }

        if (usuario.getRol() == Rol.ADMIN && nuevoRol != Rol.ADMIN) {
            long adminActivos = usuarioRepository.countByRolAndActivo(Rol.ADMIN, true);
            if (adminActivos <= 1) {
                throw new IllegalArgumentException("Debe existir al menos un usuario ADMIN activo");
            }
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setRol(nuevoRol);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return UsuarioDTO.fromEntity(usuario);
    }

    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest request) {

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioDTO activar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(true);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return UsuarioDTO.fromEntity(usuario);
    }

    @Transactional
    public UsuarioDTO desactivar(Long id, Long adminId) {

        if (id.equals(adminId)) {
            throw new IllegalArgumentException("No puedes desactivarte a ti mismo");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMIN) {
            long adminActivos = usuarioRepository.countByRolAndActivo(Rol.ADMIN, true);
            if (adminActivos <= 1) {
                throw new IllegalArgumentException("Debe existir al menos un usuario ADMIN activo");
            }
        }

        usuario.setActivo(false);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return UsuarioDTO.fromEntity(usuario);
    }
}
