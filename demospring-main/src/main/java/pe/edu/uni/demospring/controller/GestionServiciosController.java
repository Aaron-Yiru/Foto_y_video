package pe.edu.uni.demospring.controller;

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

    // Ruta donde se guardarán las imágenes
    private final Path uploadDir = Paths.get("uploads/images");

    @Autowired
    public GestionServiciosController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // =================== Mostrar página (Protegido por Spring Security) ===================
    @GetMapping
    public String mostrarGestionServicios(Model model, @RequestParam(value = "keyword", required = false) String keyword) {

        // No hace falta verificar sesión manualmente, WebSecurityConfig protege "/admin/**"

        if (keyword != null && !keyword.isEmpty()) {
            // Nota: Asegúrate de tener este método en tu Service/Repo si vas a usar el buscador
            // model.addAttribute("servicios", servicioService.buscar(keyword));
            // Por ahora usamos listar() genérico si no tienes el buscador específico implementado
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
            // Usamos timestamp para evitar nombres duplicados
            nombreArchivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path rutaArchivo = uploadDir.resolve(nombreArchivo);
            Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        }

        // Guardar en BD
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setFoto("/images/" + nombreArchivo); // Ruta relativa para el HTML

        servicioService.guardar(servicio);
        return "redirect:/admin/gestionservicios";
    }

    // =================== Eliminar servicio ===================
    @PostMapping("/eliminar")
    public String eliminarServicio(@RequestParam Long id) throws IOException {
        Servicio servicio = servicioService.buscarPorId(id);

        if (servicio != null) {
            // Eliminar imagen del disco si no es la genérica
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/") && !servicio.getFoto().contains("servicio-generico")) {
                try {
                    String nombreArchivo = servicio.getFoto().substring(8); // Quita "/images/"
                    Path rutaFoto = uploadDir.resolve(nombreArchivo);
                    Files.deleteIfExists(rutaFoto);
                } catch (Exception e) {
                    System.err.println("No se pudo borrar la imagen: " + e.getMessage());
                }
            }
            servicioService.eliminar(id);
        }
        return "redirect:/admin/gestionservicios";
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

        // Reemplazar imagen solo si el usuario subió una nueva
        if (foto != null && !foto.isEmpty()) {
            // 1. Borrar imagen anterior
            if (servicio.getFoto() != null && servicio.getFoto().startsWith("/images/")) {
                try {
                    String nombreAnterior = servicio.getFoto().substring(8);
                    Path rutaAnterior = uploadDir.resolve(nombreAnterior);
                    Files.deleteIfExists(rutaAnterior);
                } catch (Exception e) {
                    // Ignorar error de borrado
                }
            }

            // 2. Guardar nueva imagen
            String nuevoNombre = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path rutaNueva = uploadDir.resolve(nuevoNombre);
            Files.copy(foto.getInputStream(), rutaNueva, StandardCopyOption.REPLACE_EXISTING);
            servicio.setFoto("/images/" + nuevoNombre);
        }

        servicioService.guardar(servicio);
        return "redirect:/admin/gestionservicios";
    }
}






