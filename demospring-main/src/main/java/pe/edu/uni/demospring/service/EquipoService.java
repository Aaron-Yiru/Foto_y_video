package pe.edu.uni.demospring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.uni.demospring.model.Equipo;
import pe.edu.uni.demospring.repository.EquipoRepository;

import java.util.List;

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
        return equipoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        equipoRepository.deleteById(id);
    }

    // 🔍 NUEVO método de búsqueda
    public List<Equipo> buscarPorNombreOMarca(String texto) {
        return equipoRepository.findByNombreContainingIgnoreCaseOrMarcaContainingIgnoreCase(texto, texto);
    }
}