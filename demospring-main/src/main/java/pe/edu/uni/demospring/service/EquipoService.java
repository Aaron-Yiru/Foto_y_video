package pe.edu.uni.demospring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.uni.demospring.model.Equipo;
import pe.edu.uni.demospring.repository.EquipoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    public List<Equipo> listarTodos() {
        return equipoRepository.findAll();
    }

    public void guardar(Equipo equipo) {
        equipoRepository.save(equipo);
    }

    public Equipo obtenerPorId(Long id) {
        Optional<Equipo> optional = equipoRepository.findById(id);
        return optional.orElse(null);
    }

    public void eliminar(Long id) {
        equipoRepository.deleteById(id);
    }

    public List<Equipo> buscar(String texto) {
        return equipoRepository.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(texto, texto);
    }
}
