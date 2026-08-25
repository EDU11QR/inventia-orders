package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.ActualizarUsuarioRequest;
import com.edudev.pedidos_api.dto.CambiarPasswordRequest;
import com.edudev.pedidos_api.dto.CrearUsuarioRequest;
import com.edudev.pedidos_api.dto.UsuarioDTO;
import com.edudev.pedidos_api.entity.Rol;
import com.edudev.pedidos_api.entity.Usuario;
import com.edudev.pedidos_api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario(String nombre, String usuario, Rol rol, boolean activo) {
        return Usuario.builder()
                .id(1L)
                .nombre(nombre)
                .usuario(usuario)
                .passwordHash("hash")
                .rol(rol)
                .activo(activo)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private CrearUsuarioRequest crearRequest(String nombre, String usuario, String password, String rol) {
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre(nombre);
        request.setUsuario(usuario);
        request.setPassword(password);
        request.setRol(rol);
        return request;
    }

    // ==========================================
    // listar
    // ==========================================

    @Test
    void listarTodosDevuelveListaDeDTOs() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                usuario("Admin", "admin", Rol.ADMIN, true),
                usuario("Karina", "karina", Rol.VENTAS, true)
        ));

        List<UsuarioDTO> resultado = usuarioService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("admin", resultado.get(0).getUsuario());
        assertEquals("VENTAS", resultado.get(1).getRol());
    }

    // ==========================================
    // crear
    // ==========================================

    @Test
    void crearUsuarioConDatosValidos() {
        when(usuarioRepository.existsByUsuario("karina")).thenReturn(false);
        when(passwordEncoder.encode("Karina123")).thenReturn("encoded");

        CrearUsuarioRequest request = crearRequest("Karina Perez", "karina", "Karina123", "VENTAS");

        UsuarioDTO resultado = usuarioService.crear(request, 1L);

        assertEquals("Karina Perez", resultado.getNombre());
        assertEquals("karina", resultado.getUsuario());
        assertEquals("VENTAS", resultado.getRol());
        assertTrue(resultado.getActivo());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPasswordHash());
    }

    @Test
    void crearUsuarioRechazaNombreNulo() {
        CrearUsuarioRequest request = crearRequest(null, "karina", "Karina123", "VENTAS");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crear(request, 1L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuarioRechazaUsuarioDuplicado() {
        when(usuarioRepository.existsByUsuario("admin")).thenReturn(true);

        CrearUsuarioRequest request = crearRequest("Admin", "admin", "Admin123", "ADMIN");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crear(request, 1L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuarioRechazaPasswordCorta() {
        CrearUsuarioRequest request = crearRequest("Karina", "karina", "123", "VENTAS");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.crear(request, 1L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearUsuarioNormalizaUsernameAMinusculas() {
        when(usuarioRepository.existsByUsuario("karina")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        CrearUsuarioRequest request = crearRequest("Karina", "KARINA", "Karina123", "VENTAS");

        UsuarioDTO resultado = usuarioService.crear(request, 1L);

        assertEquals("karina", resultado.getUsuario());
    }

    // ==========================================
    // actualizar
    // ==========================================

    @Test
    void actualizarNombreYRol() {
        Usuario existente = usuario("Karina", "karina", Rol.VENTAS, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Karina P.");
        request.setRol("ALMACEN");

        UsuarioDTO resultado = usuarioService.actualizar(1L, request, 1L);

        assertEquals("Karina P.", resultado.getNombre());
        assertEquals("ALMACEN", resultado.getRol());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertNotNull(captor.getValue().getFechaActualizacion());
    }

    @Test
    void actualizarNoPermiteCambiarRolDelUltimoAdmin() {
        Usuario admin = usuario("Admin", "admin", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivo(Rol.ADMIN, true)).thenReturn(1L);

        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Admin");
        request.setRol("VENTAS");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, request, 1L));
    }

    // ==========================================
    // cambiarPassword
    // ==========================================

    @Test
    void cambiarPasswordConValida() {
        Usuario existente = usuario("Karina", "karina", Rol.VENTAS, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("newHash");

        CambiarPasswordRequest request = new CambiarPasswordRequest();
        request.setPassword("NuevaClave123");

        usuarioService.cambiarPassword(1L, request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("newHash", captor.getValue().getPasswordHash());
    }

    @Test
    void cambiarPasswordRechazaPasswordCorta() {
        CambiarPasswordRequest request = new CambiarPasswordRequest();
        request.setPassword("123");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cambiarPassword(1L, request));
    }

    // ==========================================
    // activar / desactivar
    // ==========================================

    @Test
    void activarUsuario() {
        Usuario desactivado = usuario("Karina", "karina", Rol.VENTAS, false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(desactivado));

        UsuarioDTO resultado = usuarioService.activar(1L);

        assertTrue(resultado.getActivo());
        verify(usuarioRepository).save(any());
    }

    @Test
    void desactivarUsuario() {
        Usuario activo = usuario("Karina", "karina", Rol.VENTAS, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(activo));

        UsuarioDTO resultado = usuarioService.desactivar(1L, 2L);

        assertFalse(resultado.getActivo());
        verify(usuarioRepository).save(any());
    }

    @Test
    void desactivarNoPermiteAutoDesactivarse() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.desactivar(1L, 1L));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desactivarNoPermiteDesactivarUltimoAdmin() {
        Usuario admin = usuario("Admin", "admin", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivo(Rol.ADMIN, true)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.desactivar(1L, 2L));
    }

    @Test
    void desactivarAdminPermiteSiHayOtrosAdmins() {
        Usuario admin = usuario("Admin", "admin", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivo(Rol.ADMIN, true)).thenReturn(2L);

        UsuarioDTO resultado = usuarioService.desactivar(1L, 2L);

        assertFalse(resultado.getActivo());
        verify(usuarioRepository).save(any());
    }
}
