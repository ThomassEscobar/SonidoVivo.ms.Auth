package cl.sonidovivo.ms.auth.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.sonidovivo.ms.auth.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA hace la magia de crear la consulta SQL automáticamente
    Optional<Usuario> findByCorreo(String correo);
}
