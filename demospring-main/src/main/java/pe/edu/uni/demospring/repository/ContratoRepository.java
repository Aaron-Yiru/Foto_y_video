package pe.edu.uni.demospring.repository;

import pe.edu.uni.demospring.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    // Hereda save(), findAll(), findById(), etc., sin escribir código.
}
