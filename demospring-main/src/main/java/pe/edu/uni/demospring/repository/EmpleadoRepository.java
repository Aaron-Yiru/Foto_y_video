package pe.edu.uni.demospring.repository;
import pe.edu.uni.demospring.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    // Buscar por nombre o cargo (para el buscador)
    List<Empleado> findByNombreContainingIgnoreCaseOrCargoContainingIgnoreCase(String nombre, String cargo);
}
