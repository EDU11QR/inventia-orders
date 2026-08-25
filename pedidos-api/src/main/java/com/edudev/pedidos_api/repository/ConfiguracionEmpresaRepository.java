package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.entity.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionEmpresaRepository
        extends JpaRepository<ConfiguracionEmpresa, Long> {
}
