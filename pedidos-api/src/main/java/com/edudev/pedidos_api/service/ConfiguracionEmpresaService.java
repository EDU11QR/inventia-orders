package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.ConfiguracionEmpresaDTO;
import com.edudev.pedidos_api.entity.ConfiguracionEmpresa;
import com.edudev.pedidos_api.repository.ConfiguracionEmpresaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfiguracionEmpresaService {

    public static final long ID_CONFIGURACION = 1L;
    public static final String NOMBRE_POR_DEFECTO = "INVENTIA";
    private static final int NOMBRE_MAXIMO_CARACTERES = 100;
    private static final int TELEFONO_MAXIMO_CARACTERES = 30;
    private static final int DIRECCION_MAXIMO_CARACTERES = 200;
    private static final int MENSAJE_MAXIMO_CARACTERES = 255;

    private final ConfiguracionEmpresaRepository repository;

    @PostConstruct
    @Transactional
    public void inicializarConfiguracion() {

        if (!repository.existsById(ID_CONFIGURACION)) {

            repository.save(ConfiguracionEmpresa.builder()
                    .id(ID_CONFIGURACION)
                    .nombreEmpresa(NOMBRE_POR_DEFECTO)
                    .telefono("")
                    .direccion("")
                    .mensajeTicket("Gracias por su compra")
                    .mostrarTelefono(false)
                    .mostrarDireccion(false)
                    .mostrarMensaje(false)
                    .fechaActualizacion(LocalDateTime.now())
                    .build()
            );
        }
    }

    // =========================================================
    // OBTENER CONFIGURACIÓN COMPLETA
    // =========================================================
    public ConfiguracionEmpresaDTO obtenerConfiguracion() {

        ConfiguracionEmpresa config =
                repository.findById(ID_CONFIGURACION)
                        .orElseGet(() -> ConfiguracionEmpresa.builder()
                                .id(ID_CONFIGURACION)
                                .nombreEmpresa(NOMBRE_POR_DEFECTO)
                                .telefono("")
                                .direccion("")
                                .mensajeTicket("Gracias por su compra")
                                .mostrarTelefono(false)
                                .mostrarDireccion(false)
                                .mostrarMensaje(false)
                                .build());

        return ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa(config.getNombreEmpresa())
                .telefono(config.getTelefono())
                .direccion(config.getDireccion())
                .mensajeTicket(config.getMensajeTicket())
                .mostrarTelefono(config.getMostrarTelefono())
                .mostrarDireccion(config.getMostrarDireccion())
                .mostrarMensaje(config.getMostrarMensaje())
                .build();
    }

    // =========================================================
    // GUARDAR CONFIGURACIÓN COMPLETA (POST)
    // =========================================================
    @Transactional
    public ConfiguracionEmpresaDTO guardarConfiguracion(
            ConfiguracionEmpresaDTO request
    ) {

        String nombreNormalizado = normalizar(request.getNombreEmpresa());

        if (nombreNormalizado.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la empresa es obligatorio"
            );
        }

        if (nombreNormalizado.length() > NOMBRE_MAXIMO_CARACTERES) {
            throw new IllegalArgumentException(
                    "El nombre de la empresa no puede superar los "
                            + NOMBRE_MAXIMO_CARACTERES + " caracteres"
            );
        }

        String telefonoNormalizado = normalizar(request.getTelefono());
        if (telefonoNormalizado.length() > TELEFONO_MAXIMO_CARACTERES) {
            throw new IllegalArgumentException(
                    "El teléfono no puede superar los "
                            + TELEFONO_MAXIMO_CARACTERES + " caracteres"
            );
        }

        String direccionNormalizada = normalizar(request.getDireccion());
        if (direccionNormalizada.length() > DIRECCION_MAXIMO_CARACTERES) {
            throw new IllegalArgumentException(
                    "La dirección no puede superar los "
                            + DIRECCION_MAXIMO_CARACTERES + " caracteres"
            );
        }

        String mensajeNormalizado = normalizar(request.getMensajeTicket());
        if (mensajeNormalizado.length() > MENSAJE_MAXIMO_CARACTERES) {
            throw new IllegalArgumentException(
                    "El mensaje del ticket no puede superar los "
                            + MENSAJE_MAXIMO_CARACTERES + " caracteres"
            );
        }

        ConfiguracionEmpresa configuracion =
                repository.findById(ID_CONFIGURACION)
                        .orElseGet(() -> ConfiguracionEmpresa.builder()
                                .id(ID_CONFIGURACION)
                                .build());

        configuracion.setNombreEmpresa(nombreNormalizado);
        configuracion.setTelefono(telefonoNormalizado);
        configuracion.setDireccion(direccionNormalizada);
        configuracion.setMensajeTicket(mensajeNormalizado);
        configuracion.setMostrarTelefono(
                request.getMostrarTelefono() != null && request.getMostrarTelefono()
        );
        configuracion.setMostrarDireccion(
                request.getMostrarDireccion() != null && request.getMostrarDireccion()
        );
        configuracion.setMostrarMensaje(
                request.getMostrarMensaje() != null && request.getMostrarMensaje()
        );
        configuracion.setFechaActualizacion(LocalDateTime.now());

        repository.save(configuracion);

        return ConfiguracionEmpresaDTO.builder()
                .nombreEmpresa(configuracion.getNombreEmpresa())
                .telefono(configuracion.getTelefono())
                .direccion(configuracion.getDireccion())
                .mensajeTicket(configuracion.getMensajeTicket())
                .mostrarTelefono(configuracion.getMostrarTelefono())
                .mostrarDireccion(configuracion.getMostrarDireccion())
                .mostrarMensaje(configuracion.getMostrarMensaje())
                .build();
    }

    // =========================================================
    // MÉTODOS DE ACCESO RÁPIDO (para ticket, dashboard, etc.)
    // =========================================================
    @Transactional(readOnly = true)
    public String obtenerNombreEmpresa() {
        return repository.findById(ID_CONFIGURACION)
                .map(ConfiguracionEmpresa::getNombreEmpresa)
                .orElse(NOMBRE_POR_DEFECTO);
    }

    @Transactional(readOnly = true)
    public String obtenerTelefono() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> c.getTelefono() != null ? c.getTelefono() : "")
                .orElse("");
    }

    @Transactional(readOnly = true)
    public String obtenerDireccion() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> c.getDireccion() != null ? c.getDireccion() : "")
                .orElse("");
    }

    @Transactional(readOnly = true)
    public String obtenerMensajeTicket() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> c.getMensajeTicket() != null ? c.getMensajeTicket() : "")
                .orElse("");
    }

    @Transactional(readOnly = true)
    public boolean debeMostrarTelefono() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> Boolean.TRUE.equals(c.getMostrarTelefono()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean debeMostrarDireccion() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> Boolean.TRUE.equals(c.getMostrarDireccion()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean debeMostrarMensaje() {
        return repository.findById(ID_CONFIGURACION)
                .map(c -> Boolean.TRUE.equals(c.getMostrarMensaje()))
                .orElse(false);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }
}
