package pe.edu.uni.demospring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    @Autowired
    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    // ✅ Listar todos los servicios
    public List<Servicio> listar() {
        return servicioRepository.findAll();
    }

    // ✅ Guardar (crear o actualizar) un servicio
    public void guardar(Servicio servicio) {
        servicioRepository.save(servicio);
    }

    // ✅ Eliminar servicio por ID
    public void eliminar(Long id) {
        servicioRepository.deleteById(id);
    }

    // ✅ Buscar un servicio por su ID
    public Servicio buscarPorId(Long id) {
        return servicioRepository.findById(id).orElse(null);
    }
}

