package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    // Inyección de dependencia del Repositorio
    private final ServicioRepository servicioRepository;

    @Autowired
    public ServicioService(ServicioRepository servicioRepository) {
        // Asigna el repositorio, Spring se encarga de inyectarlo
        this.servicioRepository = servicioRepository;
    }

    /**
     * Lista todos los servicios disponibles de la Base de Datos.
     * @return Lista de Servicios.
     */
    public List<Servicio> listar() {
        return servicioRepository.findAll();
    }

    /**
     * Guarda un nuevo servicio o actualiza uno existente en la Base de Datos.
     * @param servicio El objeto Servicio a guardar.
     */
    public void agregar(Servicio servicio) {
        servicioRepository.save(servicio);
    }

    /**
     * Elimina un servicio por su ID de la Base de Datos.
     * @param id El ID del servicio a eliminar.
     */
    public void eliminar(Long id) {
        servicioRepository.deleteById(id);
    }

    /**
     * Busca un servicio por su ID en la Base de Datos.
     * @param id El ID del servicio a buscar.
     * @return El objeto Servicio o null si no se encuentra.
     */
    public Servicio buscarPorId(Long id) {
        return servicioRepository.findById(id).orElse(null);
    }
}