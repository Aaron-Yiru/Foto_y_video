package pe.edu.uni.demospring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class LoginController {

    // NOTA: Estas constantes simulan credenciales fijas para el administrador.
    // En un sistema real, estas deberían estar en application.properties o en la base de datos
    // y la contraseña DEBE estar hasheada.
    private static final String ADMIN_EMAIL = "admin@uni.edu.pe";
    private static final String ADMIN_PASSWORD = "12345";

    // Mostrar página de admin
    @GetMapping
    public String mostrarAdmin(HttpSession session, Model model) {
        // Si ya está logueado, mostrar dashboard
        Boolean adminLogueado = (Boolean) session.getAttribute("adminLogueado");
        if (adminLogueado != null && adminLogueado) {
            String adminEmail = (String) session.getAttribute("adminEmail");
            model.addAttribute("adminEmail", adminEmail);
        }

        return "admin";
    }


    @PostMapping("/login")
    public String loginAdmin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("🔐 Intento de login admin - Email: " + email);

        // Validar credenciales
        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            // Login exitoso
            session.setAttribute("adminLogueado", true);
            session.setAttribute("adminEmail", email);

            System.out.println("✅ Admin logueado exitosamente: " + email);

            redirectAttributes.addFlashAttribute("mensaje", "¡Bienvenido administrador!");
            return "redirect:/admin";

        } else {
            // Login fallido
            System.out.println("❌ Credenciales incorrectas para: " + email);

            redirectAttributes.addFlashAttribute("error",
                    "Credenciales incorrectas. Intenta nuevamente.");
            return "redirect:/admin";
        }
    }


    @GetMapping("/logout")
    public String logoutAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        String adminEmail = (String) session.getAttribute("adminEmail");

        // Limpiar sesión
        session.removeAttribute("adminLogueado");
        session.removeAttribute("adminEmail");

        System.out.println("👋 Admin cerró sesión: " + adminEmail);

        redirectAttributes.addFlashAttribute("mensaje", "Sesión cerrada correctamente");
        return "redirect:/admin";
    }
}
