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

    /**
     * Guarda la solicitud de contrato en la base de datos.
     * @param contrato El objeto Contrato con los datos del formulario.
     * @return El Contrato guardado, con el ID asignado.
     */
    public Contrato guardarContrato(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    /**
     * Obtiene todos los contratos registrados. Usado por el Administrador.
     * @return Una lista de todos los Contratos.
     */
    public List<Contrato> obtenerTodos() {
        return contratoRepository.findAll();
    }

    public void guardar(Contrato contrato) {
    }
}
