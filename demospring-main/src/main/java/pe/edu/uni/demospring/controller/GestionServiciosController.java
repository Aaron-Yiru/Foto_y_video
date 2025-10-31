package pe.edu.uni.demospring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.io.IOException;
import java.nio.file.*;

@Controller
public class GestionServiciosController {

    @Autowired
    private ServicioRepository servicioRepository;

    private final Path uploadDir = Paths.get("uploads/images");

    // =================== Mostrar página ===================
    // =================== Mostrar página con búsqueda ===================
    @GetMapping("/gestionservicios")
    public String mostrarGestionServicios(
            HttpSession session,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/admin";
        }

        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("servicios",
                    servicioRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(keyword, keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("servicios", servicioRepository.findAll());
        }

        model.addAttribute("servicio", new Servicio());
        return "gestionservicios";
    }


    // =================== Agregar servicio ===================
    @PostMapping("/gestionservicios/agregar")
    public String agregarServicio(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam("foto") MultipartFile foto
    ) throws IOException {

        // Crear carpeta si no existe
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Guardar imagen
        String nombreArchivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
        Path rutaArchivo = uploadDir.resolve(nombreArchivo);
        Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        // Guardar datos en BD
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setFoto("/images/" + nombreArchivo);

        servicioRepository.save(servicio);
        return "redirect:/gestionservicios";
    }

    // =================== Eliminar servicio ===================
    @PostMapping("/gestionservicios/eliminar")
    public String eliminarServicio(@RequestParam Long id) throws IOException {
        Servicio servicio = servicioRepository.findById(id).orElse(null);
        if (servicio != null) {
            // Eliminar imagen del disco
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/")) {
                Path rutaFoto = uploadDir.resolve(servicio.getFoto().substring(8)); // remove "/images/"
                Files.deleteIfExists(rutaFoto);
            }
            servicioRepository.deleteById(id);
        }
        return "redirect:/gestionservicios";
    }

    // =================== Mostrar formulario de edición ===================
    @GetMapping("/gestionservicios/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Servicio servicio = servicioRepository.findById(id).orElse(null);
        if (servicio == null) {
            return "redirect:/gestionservicios";
        }
        model.addAttribute("servicio", servicio);
        return "editarservicio";
    }

    // =================== Procesar edición ===================
    @PostMapping("/gestionservicios/editar")
    public String editarServicio(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) throws IOException {

        Servicio servicio = servicioRepository.findById(id).orElse(null);
        if (servicio == null) {
            return "redirect:/gestionservicios";
        }

        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);

        // Si se sube una nueva imagen, reemplazar
        if (foto != null && !foto.isEmpty()) {
            // Borrar la anterior
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/")) {
                Path rutaAnterior = uploadDir.resolve(servicio.getFoto().substring(8));
                Files.deleteIfExists(rutaAnterior);
            }

            // Guardar la nueva
            String nuevoNombre = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path rutaNueva = uploadDir.resolve(nuevoNombre);
            Files.copy(foto.getInputStream(), rutaNueva, StandardCopyOption.REPLACE_EXISTING);
            servicio.setFoto("/images/" + nuevoNombre);
        }

        servicioRepository.save(servicio);
        return "redirect:/gestionservicios";
    }
}
