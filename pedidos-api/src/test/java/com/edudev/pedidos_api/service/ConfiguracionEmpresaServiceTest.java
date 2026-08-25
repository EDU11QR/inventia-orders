package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.ConfiguracionEmpresaDTO;
import com.edudev.pedidos_api.entity.ConfiguracionEmpresa;
import com.edudev.pedidos_api.repository.ConfiguracionEmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionEmpresaServiceTest {

    @Mock
    private ConfiguracionEmpresaRepository repository;

    @InjectMocks
    private ConfiguracionEmpresaService service;

    private ConfiguracionEmpresa configuracion(String nombre) {

        return ConfiguracionEmpresa.builder()
                .id(1L)
                .nombreEmpresa(nombre)
                .telefono("")
                .direccion("")
                .mensajeTicket("Gracias por su compra")
                .mostrarTelefono(false)
                .mostrarDireccion(false)
                .mostrarMensaje(false)
                .build();
    }

    // ==========================================
    // inicialización automática
    // ==========================================

    @Test
    void inicializaConNombrePorDefectoSiLaTablaEstaVacia() {

        when(repository.existsById(1L)).thenReturn(false);

        service.inicializarConfiguracion();

        ArgumentCaptor<ConfiguracionEmpresa> captor =
                ArgumentCaptor.forClass(ConfiguracionEmpresa.class);

        verify(repository).save(captor.capture());

        assertEquals(1L, captor.getValue().getId());

        assertEquals("INVENTIA",
                captor.getValue().getNombreEmpresa());
    }

    @Test
    void noReiniciaSiYaExisteConfiguracion() {

        when(repository.existsById(1L)).thenReturn(true);

        service.inicializarConfiguracion();

        verify(repository, never()).save(any());
    }

    // ==========================================
    // lectura
    // ==========================================

    @Test
    void obtenerConfiguracionDevuelveElNombreVigente() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(configuracion("LADY POSH")));

        ConfiguracionEmpresaDTO configuracion =
                service.obtenerConfiguracion();

        assertEquals("LADY POSH",
                configuracion.getNombreEmpresa());
    }

    @Test
    void obtenerNombreEmpresaCaeAlDefaultSiNoHayRegistro() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertEquals("INVENTIA", service.obtenerNombreEmpresa());
    }

    // ==========================================
    // actualización y validaciones
    // ==========================================

    @Test
    void guardarConfiguracionAplicaTrimYPersiste() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(configuracion("INVENTIA")));

        ConfiguracionEmpresaDTO request = ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa("   LADY POSH   ")
                .telefono("")
                .direccion("")
                .mensajeTicket("Gracias por su compra")
                .mostrarTelefono(false)
                .mostrarDireccion(false)
                .mostrarMensaje(false)
                .build();

        service.guardarConfiguracion(request);

        ArgumentCaptor<ConfiguracionEmpresa> captor =
                ArgumentCaptor.forClass(ConfiguracionEmpresa.class);

        verify(repository).save(captor.capture());

        assertEquals("LADY POSH",
                captor.getValue().getNombreEmpresa());
    }

    @Test
    void rechazaNombreNuloOBlanco() {

        ConfiguracionEmpresaDTO requestNulo = ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa(null)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarConfiguracion(requestNulo));

        ConfiguracionEmpresaDTO requestBlanco = ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa("   ")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarConfiguracion(requestBlanco));

        verify(repository, never()).save(any());
    }

    @Test
    void rechazaNombreMayorACienCaracteres() {

        String nombreLargo = "x".repeat(101);

        ConfiguracionEmpresaDTO request = ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa(nombreLargo)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarConfiguracion(request));

        verify(repository, never()).save(any());
    }
}
