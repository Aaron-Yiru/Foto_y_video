package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;

    @Autowired
    public ContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    public Contrato guardarContrato(Contrato contrato) {
        return contratoRepository.save(contrato); // Utiliza el método de Spring Data JPA
    }

    public List<Contrato> obtenerTodos() {
        return contratoRepository.findAll(); // Método para la gestión de contratos
    }
}
