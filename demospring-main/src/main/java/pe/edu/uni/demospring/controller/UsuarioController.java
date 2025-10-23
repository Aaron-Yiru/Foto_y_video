package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/gestionusuarios")
    public String mostrarUsuarios(Model model) {
        // ✅ Traer todos los usuarios desde la base de datos
        List<Usuario> usuarios = usuarioService.obtenerTodos();

        // ✅ Contar usuarios activos e inactivos directamente con métodos del repositorio
        long usuariosActivos = usuarioService.contarPorEstado(true);
        long usuariosInactivos = usuarioService.contarPorEstado(false);
        long totalUsuarios = usuarioService.contarUsuarios();

        // ✅ Enviar los datos a la vista (Thymeleaf)
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("usuariosInactivos", usuariosInactivos);
        model.addAttribute("totalUsuarios", totalUsuarios);

        return "gestionusuarios"; // Nombre del HTML
    }
}
