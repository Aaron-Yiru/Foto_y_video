package pe.edu.uni.demospring.repository;

import pe.edu.uni.demospring.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     * Spring Security usará esto para cargar los datos y luego comparará
     * la contraseña internamente usando BCrypt.
     */
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    long countByActivoFalse();
}