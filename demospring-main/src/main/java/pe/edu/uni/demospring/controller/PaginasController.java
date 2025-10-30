package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class PaginasController {

    // ❌ ELIMINADA cualquier referencia a una lista estática de servicios.

    private final ServicioService servicioService; // ✅ Inyección de dependencia

    @Autowired
    public PaginasController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // ===================================
    // Vistas Públicas Generales
    // ===================================

    @GetMapping("/index")
    public String index(Model model) {
        // Asumiendo que /index solo devuelve la vista
        return "index";
    }

    @GetMapping("/galeria")
    public String galeria() {
        return "galeria";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    // ===================================
    // Vista de Servicios (Catálogo)
    // ===================================

    @GetMapping("/servicios")
    public String mostrarServicios(Model model) {
        // ✅ Obtiene la lista de servicios directamente desde el ServicioService
        // Esto garantiza que la vista muestre los datos del catálogo en memoria (ServicioService)
        // y resuelve la incoherencia de las listas estáticas.
        model.addAttribute("servicios", servicioService.listar());
        return "servicios";
    }

    // Nota: La ruta '/' suele redirigir a '/index' en muchas configuraciones.
    // Si necesitas que '/' también funcione:
    @GetMapping
    public String rutaRaiz() {
        return "redirect:/index";
    }
}




