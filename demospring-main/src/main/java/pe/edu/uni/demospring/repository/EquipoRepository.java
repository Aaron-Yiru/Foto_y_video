package pe.edu.uni.demospring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.uni.demospring.model.Equipo;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByNombreContainingIgnoreCaseOrMarcaContainingIgnoreCase(String nombre, String marca);
}
