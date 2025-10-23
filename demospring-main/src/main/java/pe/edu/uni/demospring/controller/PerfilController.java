
package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService; // Usamos final y constructor para inyección

    @Autowired
    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // NOTA: La lista estática de usuariosRegistrados fue ELIMINADA.

    @GetMapping
    public String mostrarPerfil() {
        return "perfil";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "perfil";
        }

        // Usa el servicio para verificar la existencia del email en la DB
        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "Este correo electrónico ya está registrado.");
            return "perfil";
        }

        // Crea y guarda el nuevo usuario usando el servicio (persistencia en DB)
        Usuario nuevoUsuario = new Usuario(nombre, email, password);
        usuarioService.registrarUsuario(nuevoUsuario);

        model.addAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.");
        return "perfil";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Busca el usuario en la base de datos
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();

            // Verificación de contraseña (idealmente, usando un PasswordEncoder en un proyecto real)
            if (u.getPassword().equals(password)) {
                // Login exitoso: guarda datos en la sesión
                session.setAttribute("usuarioLogueado", true);
                session.setAttribute("nombreUsuario", u.getNombre());
                session.setAttribute("emailUsuario", u.getEmail());
                return "redirect:/sesion";
            }
        }

        // Login fallido
        model.addAttribute("error", "Correo o contraseña incorrectos.");
        return "perfil";
    }

    // ... (Mantén el método logout si existe en tu controlador)
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        System.out.println(" Sesión cerrada para: " + nombreUsuario);
        session.invalidate();
        return "redirect:/perfil";
    }
}
