package pe.edu.uni.demospring.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.edu.uni.demospring.security.MyUserPrincipal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // El acceso a esta ruta ya está protegido por WebSecurityConfig (.hasRole("ADMIN"))
    // No hace falta ningún 'if' para validar la sesión.

    @GetMapping
    public String mostrarDashboard(Model model, @AuthenticationPrincipal MyUserPrincipal principal) {
        // 'principal' contiene los datos del usuario logueado
        if (principal != null) {
            model.addAttribute("adminEmail", principal.getUsername());
        }
        return "admin"; // Retorna la vista admin.html
    }
}