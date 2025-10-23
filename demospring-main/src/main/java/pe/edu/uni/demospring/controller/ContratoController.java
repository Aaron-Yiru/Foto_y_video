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

    @GetMapping("/verContratos")
    public String verContratos(Model model, HttpSession session) {
        // ELIMINADO: Verificación de admin - ahora cualquiera puede ver para propósitos de demostración
        // Si quieres mantener seguridad, comenta las siguientes 3 líneas:
        // if (session.getAttribute("adminLogueado") == null) {
        //     return "redirect:/login";
        // }

        // Obtiene contratos pendientes de aprobación desde el contexto global
        List<Contrato> contratosPendientes = (List<Contrato>) session.getServletContext()
                .getAttribute("contratosPendientes");

        if (contratosPendientes == null) {
            contratosPendientes = new ArrayList<>();
        }

        model.addAttribute("contratos", contratosPendientes);
        model.addAttribute("totalContratos", contratosPendientes.size());

        return "lista-contratos";
    }

    @PostMapping("/contratos/subir")
    public String subirContratos(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/login";
        }

        List<Contrato> contratosPendientes = (List<Contrato>) session.getServletContext()
                .getAttribute("contratosPendientes");

        if (contratosPendientes == null || contratosPendientes.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No hay contratos pendientes para subir");
            return "redirect:/verContratos";
        }

        try {
            // Guardar los contratos en la base de datos
            for (Contrato contrato : contratosPendientes) {
                contratoService.guardar(contrato);
            }

            int cantidadGuardada = contratosPendientes.size();

            // Limpiar los contratos pendientes después de guardarlos
            session.getServletContext().removeAttribute("contratosPendientes");

            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Contratos aprobados y guardados exitosamente! Se guardaron " +
                            cantidadGuardada + " contrato(s) en la base de datos.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar los contratos: " + e.getMessage());
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

        return "lista-contratos-guardados";
    }
}