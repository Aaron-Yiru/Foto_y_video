package pe.edu.uni.demospring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.service.ServicioService;

import java.io.IOException;
import java.nio.file.*;

@Controller
@RequestMapping("/admin/gestionservicios")
public class GestionServiciosController {

    private final ServicioService servicioService;
    private final Path uploadDir = Paths.get("uploads/images");

    @Autowired
    public GestionServiciosController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // =================== Mostrar página ===================
    @GetMapping
    public String mostrarGestionServicios(
            HttpSession session,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        // Verifica sesión del admin
        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/admin";
        }

        // Búsqueda opcional
        if (keyword != null && !keyword.isEmpty()) {
            // Si quieres implementar búsqueda, añade un método en tu repositorio:
            // findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase
            model.addAttribute("servicios", servicioService.listar());
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("servicios", servicioService.listar());
        }

        model.addAttribute("servicio", new Servicio());
        return "gestionservicios";
    }

    // =================== Agregar servicio ===================
    @PostMapping("/agregar")
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
        String nombreArchivo = "servicio-generico.jpg";
        if (!foto.isEmpty()) {
            nombreArchivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path rutaArchivo = uploadDir.resolve(nombreArchivo);
            Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        }

        // Guardar en BD
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setFoto("/images/" + nombreArchivo);

        servicioService.guardar(servicio);
        return "redirect:/admin/gestionservicios";
    }

    // =================== Eliminar servicio ===================
    @PostMapping("/eliminar")
    public String eliminarServicio(@RequestParam Long id) throws IOException {
        Servicio servicio = servicioService.buscarPorId(id);
        if (servicio != null) {
            // Eliminar imagen del disco (si existe)
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/")) {
                Path rutaFoto = uploadDir.resolve(servicio.getFoto().substring(8));
                Files.deleteIfExists(rutaFoto);
            }
            servicioService.eliminar(id);
        }
        return "redirect:/admin/gestionservicios";
    }

    // =================== Mostrar formulario de edición ===================
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Servicio servicio = servicioService.buscarPorId(id);
        if (servicio == null) {
            return "redirect:/admin/gestionservicios";
        }
        model.addAttribute("servicio", servicio);
        return "editarservicio"; // Página separada para edición
    }

    // =================== Procesar edición ===================
    @PostMapping("/editar")
    public String editarServicio(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) throws IOException {

        Servicio servicio = servicioService.buscarPorId(id);
        if (servicio == null) {
            return "redirect:/admin/gestionservicios";
        }

        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);

        // Reemplazar imagen si se sube una nueva
        if (foto != null && !foto.isEmpty()) {
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/")) {
                Path rutaAnterior = uploadDir.resolve(servicio.getFoto().substring(8));
                Files.deleteIfExists(rutaAnterior);
            }

            String nuevoNombre = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path rutaNueva = uploadDir.resolve(nuevoNombre);
            Files.copy(foto.getInputStream(), rutaNueva, StandardCopyOption.REPLACE_EXISTING);
            servicio.setFoto("/images/" + nuevoNombre);
        }

        servicioService.guardar(servicio);
        return "redirect:/admin/gestionservicios";
    }

}






