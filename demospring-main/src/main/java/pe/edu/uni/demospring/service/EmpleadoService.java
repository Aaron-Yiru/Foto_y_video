package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.Empleado;
import pe.edu.uni.demospring.repository.EmpleadoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public void guardar(Empleado empleado) {
        empleadoRepository.save(empleado);
    }

    public Empleado obtenerPorId(Long id) {
        Optional<Empleado> optional = empleadoRepository.findById(id);
        return optional.orElse(null);
    }

    public void eliminar(Long id) {
        empleadoRepository.deleteById(id);
    }

    public List<Empleado> buscar(String texto) {
        return empleadoRepository.findByNombreContainingIgnoreCaseOrCargoContainingIgnoreCase(texto, texto);
    }
}
