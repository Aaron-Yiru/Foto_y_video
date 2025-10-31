package pe.edu.uni.demospring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import pe.edu.uni.demospring.model.Servicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.util.Map;

@Controller
@RequestMapping("/")
public class PaginasController {

    // Lista de servicios disponibles (usada para la vista servicios.html)
    private static final Map<Long, Servicio> serviciosDisponibles = Map.of(
            1L, new Servicio(1L, "Video y Fotografía de Eventos", "Cobertura completa de bodas, bautizos, etc.", 500.00),
            2L, new Servicio(2L, "Producción de Video", "Creación de contenido audiovisual corporativo y publicitario.", 800.00),
            3L, new Servicio(3L, "Video y Fotografía con Drones", "Tomas aéreas impresionantes para eventos y propiedades.", 650.00)
    );

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @Autowired
    private ServicioRepository servicioRepository; // ✅ Inyección del repositorio


    @GetMapping("/servicios")
    public String mostrarServicios(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll()); // ✅ llamado correcto
        return "servicios";
    }
    @GetMapping("/galeria")
    public String galeria() {
        return "galeria";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }
}




