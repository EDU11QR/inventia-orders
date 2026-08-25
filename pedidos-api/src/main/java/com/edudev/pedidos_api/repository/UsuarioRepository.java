package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.entity.Rol;
import com.edudev.pedidos_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    long countByRolAndActivo(Rol rol, boolean activo);
}
