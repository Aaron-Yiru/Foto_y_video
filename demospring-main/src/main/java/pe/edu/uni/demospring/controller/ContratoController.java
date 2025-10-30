package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.service.ContratoService;
import pe.edu.uni.demospring.model.Contrato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;

@Controller
public class ContratoController {

    private final ContratoService contratoService;

    @Autowired
    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    // NOTA: Los métodos /guardarContrato y @ModelAttribute fueron ELIMINADOS,
    // ya que la lógica de contratación se movió a SesionController.

    // Código que debe estar en el controlador que maneja /verContratos
    @GetMapping("/verContratos")
    public String verContratos(Model model) {
        // 1. Obtener todos los contratos de la DB
        List<Contrato> contratos = contratoService.obtenerTodos(); // Debe retornar los contratos guardados

        // 2. Pasar datos al modelo
        model.addAttribute("contratos", contratos); // <--- CLAVE CRÍTICA
        model.addAttribute("totalContratos", contratos.size());

        // 3. Retornar la vista corregida
        return "lista-contratos"; // Debe coincidir con el nombre de tu archivo HTML
    }

    /**
     * ✅ Sube los contratos pendientes desde memoria a la base de datos (Supabase)
     */
    @PostMapping("/contratos/subir")
    public String subirContratos(HttpSession session, RedirectAttributes ra) {

        // 🔐 Solo un administrador puede subir contratos
        if (session.getAttribute("adminLogueado") == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión como administrador para aprobar contratos.");
            return "redirect:/login";
        }

        @SuppressWarnings("unchecked")
        List<Contrato> contratosPendientes = (List<Contrato>) session.getServletContext()
                .getAttribute("contratosPendientes");

        if (contratosPendientes == null || contratosPendientes.isEmpty()) {
            ra.addFlashAttribute("error", "⚠️ No hay contratos pendientes para subir.");
            return "redirect:/verContratos";
        }

        try {
            // 🔹 Guardar los contratos en la base de datos
            for (Contrato contrato : contratosPendientes) {
                contratoService.guardarContrato(contrato);
            }

            int cantidadGuardada = contratosPendientes.size();

            // 🧹 Limpiar los contratos pendientes del contexto global
            session.getServletContext().removeAttribute("contratosPendientes");

            ra.addFlashAttribute("mensaje",
                    "✅ ¡Contratos aprobados y guardados exitosamente! Se guardaron "
                            + cantidadGuardada + " contrato(s) en la base de datos.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ Error al guardar los contratos: " + e.getMessage());
        }

        return "redirect:/verContratos";
    }


    // Método adicional para ver todos los contratos guardados en BD
    @GetMapping("/contratos/guardados")
    public String verContratosGuardados(Model model, HttpSession session) {
        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/login";
        }

        // Obtiene la lista completa de contratos desde la base de datos
        List<Contrato> contratosGuardados = contratoService.obtenerTodos();
        model.addAttribute("contratos", contratosGuardados);
        model.addAttribute("totalContratos", contratosGuardados.size());

        return "lista-contratos";
    }

}