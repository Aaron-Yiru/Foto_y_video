package pe.edu.uni.demospring.repository;

import pe.edu.uni.demospring.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    //  Nuevo método para buscar por email del usuario
    List<Contrato> findByEmailOrderByIdDesc(String email);
}
