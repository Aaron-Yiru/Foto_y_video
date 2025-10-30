package pe.edu.uni.demospring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uni.demospring.model.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
}
