package pe.edu.uni.demospring.repository;

import pe.edu.uni.demospring.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    // Spring Data JPA ya proporciona findById, findAll, save, etc.
}
