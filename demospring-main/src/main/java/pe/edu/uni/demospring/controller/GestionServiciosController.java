package pe.edu.uni.demospring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.uni.demospring.model.Servicio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GestionServiciosController {

    // Lista compartida entre admin y vista pública
    static final List<Servicio> listaServicios = new ArrayList<>();

    static {
        listaServicios.add(new Servicio(1L, "Video y Fotografía de Eventos", "Cobertura completa de bodas, bautizos, etc.", 500.00, "/img/servicios/evento.jpg"));
        listaServicios.add(new Servicio(2L, "Producción de Video", "Creación de contenido audiovisual corporativo y publicitario.", 800.00, "/img/servicios/video1.jpg"));
        listaServicios.add(new Servicio(3L, "Video y Fotografía con Drones", "Tomas aéreas impresionantes para eventos y propiedades.", 650.00, "/img/servicios/dron.jpg"));
    }



    // --- PANEL DE ADMINISTRACIÓN ---
    @GetMapping("/admin/gestionservicios")
    public String mostrarPaginaAdmin(Model model) {
        model.addAttribute("servicios", listaServicios);
        return "gestionservicios";
    }

    @PostMapping("/admin/gestionservicios/agregar")
    public String agregarServicio(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam("foto") MultipartFile foto
    ) throws IOException {

        // 📁 Carpeta donde se guardarán las imágenes (fuera de /static)
        String uploadDir = "uploads/images/";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(uploadDir));

        // 📸 Nombre del archivo
        String nombreArchivo = foto.getOriginalFilename();

        // ✅ Guardar físicamente la imagen en el disco
        if (nombreArchivo != null && !foto.isEmpty()) {
            java.nio.file.Path rutaArchivo = java.nio.file.Paths.get(uploadDir + nombreArchivo);
            java.nio.file.Files.copy(foto.getInputStream(), rutaArchivo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 🌐 Ruta pública accesible desde el navegador
        String rutaWeb = "/images/" + nombreArchivo;

        // 🆕 Agregar el nuevo servicio con su imagen real
        listaServicios.add(new Servicio(
                (long) (listaServicios.size() + 1),
                nombre,
                descripcion,
                precio,
                rutaWeb
        ));

        return "redirect:/admin/gestionservicios";
    }


    @PostMapping("/admin/gestionservicios/eliminar")
    public String eliminarServicio(@RequestParam Long id) {
        listaServicios.removeIf(s -> s.getId().equals(id));
        return "redirect:/admin/gestionservicios";
    }

}
