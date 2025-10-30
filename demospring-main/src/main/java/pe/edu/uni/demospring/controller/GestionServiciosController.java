package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.service.ServicioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/admin/gestionservicios")
public class GestionServiciosController {

    private final ServicioService servicioService;

    // Directorio donde se guardarán las imágenes (asumiendo que está configurado)
    // NOTA: Para archivos estáticos, la ruta 'static/images' es generalmente mejor.
    private static final String UPLOAD_DIR = "src/main/resources/static/images";

    @Autowired
    public GestionServiciosController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // ===========================================
    // 1. Mostrar Catálogo (READ)
    // ===========================================

    @GetMapping
    public String mostrarGestion(Model model) {
        // ✅ Obtener la lista del catálogo usando el Servicio (desde la DB)
        model.addAttribute("servicios", servicioService.listar());
        return "gestionservicios";
    }

    // ===========================================
    // 2. Agregar Servicio (CREATE)
    // ===========================================

    @PostMapping("/agregar")
    public String agregarServicio(
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") double precio,
            // ⭐ CORRECCIÓN CLAVE: La variable local se llama 'foto'
            @RequestParam("foto") MultipartFile foto,
            RedirectAttributes ra) {

        String nombreArchivo = "servicio-generico.jpg"; // Default

        // Usar la variable 'foto'
        if (!foto.isEmpty()) {
            try {
                // Asegurar que el nombre del archivo no sea nulo antes de operar
                String originalFilename = foto.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    ra.addFlashAttribute("error", "Error: Nombre de archivo no encontrado.");
                    return "redirect:/admin/gestionservicios";
                }

                // Generar un nombre único para el archivo
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                nombreArchivo = UUID.randomUUID().toString() + extension;

                // Definir la ruta de destino
                Path filePath = Paths.get(UPLOAD_DIR, nombreArchivo);

                // Guardar el archivo en el sistema de archivos
                Files.write(filePath, foto.getBytes()); // Usar 'foto'

            } catch (IOException e) {
                ra.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
                return "redirect:/admin/gestionservicios";
            }
        }

        // ✅ Crear el servicio con la ruta web de la imagen
        String rutaWebFoto = "/images/" + nombreArchivo;

        // El ID es nulo; el repositorio se encargará de asignarle el ID
        Servicio nuevoServicio = new Servicio(nombre, descripcion, precio, rutaWebFoto);

        // ✅ Usar el servicio para agregar
        servicioService.agregar(nuevoServicio);

        ra.addFlashAttribute("mensaje", "Servicio '" + nombre + "' agregado exitosamente.");
        return "redirect:/admin/gestionservicios";
    }

    // ===========================================
    // 3. Eliminar Servicio (DELETE)
    // ===========================================

    @PostMapping("/eliminar")
    // Se cambia de @PathVariable a @RequestParam para coincidir con la plantilla HTML
    public String eliminarServicio(@RequestParam Long id, RedirectAttributes ra) {
        servicioService.eliminar(id);
        ra.addFlashAttribute("mensaje", "Servicio con ID " + id + " eliminado.");
        return "redirect:/admin/gestionservicios";
    }
}
