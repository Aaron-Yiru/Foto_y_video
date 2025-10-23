package pe.edu.uni.demospring.repository;

import pe.edu.uni.demospring.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     * Es clave para el proceso de inicio de sesión.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con un correo electrónico dado,
     * ignorando mayúsculas y minúsculas. Es clave para el registro.
     */
    boolean existsByEmailIgnoreCase(String email);



    Usuario findByEmailAndPassword(String email, String password);

    long countByActivoTrue();

    long countByActivoFalse();
}