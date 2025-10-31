package pe.edu.uni.demospring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.uni.demospring.model.Servicio;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // 🔍 Buscar por nombre o descripción (ignorando mayúsculas/minúsculas)
    List<Servicio> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);
}