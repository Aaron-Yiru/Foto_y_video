package pe.edu.uni.demospring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.uni.demospring.model.Rol;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Busca un rol por su nombre exacto (ej: "ROLE_ADMIN")
    Optional<Rol> findByNombre(String nombre);
}
