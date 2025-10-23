package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.model.Servicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sesion")
public class SesionController {

    // Lista de servicios disponibles (simulando datos de la DB)
    private static final Map<Long, Servicio> serviciosDisponibles = Map.of(
            1L, new Servicio(1L, "Video y Fotografía de Eventos", "Cobertura completa de bodas, bautizos, etc.", 500.00),
            2L, new Servicio(2L, "Producción de Video", "Creación de contenido audiovisual corporativo y publicitario.", 800.00),
            3L, new Servicio(3L, "Video y Fotografía con Drones", "Tomas aéreas impresionantes para eventos y propiedades.", 650.00)
    );

    @GetMapping
    public String mostrarSesion(Model model, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/perfil";
        }

        // --- Lógica del Carrito ---
        @SuppressWarnings("unchecked")
        List<Servicio> carrito = (List<Servicio>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        double totalPagar = carrito.stream().mapToDouble(Servicio::getPrecio).sum();

        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        model.addAttribute("emailUsuario", session.getAttribute("emailUsuario"));
        model.addAttribute("servicios", carrito);
        model.addAttribute("totalPagar", String.format("%.2f", totalPagar));
        // --------------------------

        return "sesion";
    }

    @PostMapping("/agregar-servicio")
    public String agregarServicio(@RequestParam("servicioId") Long servicioId, HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("usuarioLogueado") == null) {
            // Requiere iniciar sesión antes de agregar al carrito
            ra.addFlashAttribute("error", "Necesitas iniciar sesión para contratar servicios.");
            return "redirect:/perfil";
        }

        Servicio servicio = serviciosDisponibles.get(servicioId);
        if (servicio != null) {
            @SuppressWarnings("unchecked")
            List<Servicio> carrito = (List<Servicio>) session.getAttribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<>();
            }
            carrito.add(servicio);
            session.setAttribute("carrito", carrito);
            ra.addFlashAttribute("mensaje", "Servicio '" + servicio.getNombre() + "' añadido al carrito.");
        }

        // Redirigir a servicios para que pueda agregar más
        return "redirect:/servicios";
    }

    @PostMapping("/eliminar-servicio")
    public String eliminarServicio(@RequestParam("servicioIndex") int servicioIndex, HttpSession session, RedirectAttributes ra) {
        @SuppressWarnings("unchecked")
        List<Servicio> carrito = (List<Servicio>) session.getAttribute("carrito");

        if (carrito != null && servicioIndex >= 0 && servicioIndex < carrito.size()) {
            String nombreServicio = carrito.remove(servicioIndex).getNombre();
            session.setAttribute("carrito", carrito);
            ra.addFlashAttribute("mensaje", "Servicio '" + nombreServicio + "' eliminado del carrito.");
        }

        return "redirect:/sesion";
    }

    @PostMapping("/procesar-pago")
    public String procesarPago(
            @RequestParam("telefono") String telefono,
            @RequestParam("fechaEvento") String fechaEvento,
            @RequestParam("lugar") String lugar,
            @RequestParam(value = "comentarios", required = false, defaultValue = "") String comentarios,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obtener datos del usuario desde la sesión
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (nombreUsuario == null || emailUsuario == null) {
            redirectAttributes.addFlashAttribute("error", "Sesión expirada. Por favor inicia sesión nuevamente.");
            return "redirect:/perfil";
        }

        // Obtener servicios del carrito (usar "carrito" en lugar de "servicios")
        @SuppressWarnings("unchecked")
        List<Servicio> servicios = (List<Servicio>) session.getAttribute("carrito");

        // Validar que hay servicios en el carrito
        if (servicios == null || servicios.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No hay servicios en el carrito");
            return "redirect:/sesion";
        }

        // Obtener o crear la lista de contratos pendientes globales (para el admin)
        @SuppressWarnings("unchecked")
        List<Contrato> contratosPendientes = (List<Contrato>) session.getServletContext()
                .getAttribute("contratosPendientes");
        if (contratosPendientes == null) {
            contratosPendientes = new ArrayList<>();
        }

        // Crear un contrato por cada servicio contratado
        for (Servicio servicio : servicios) {
            Contrato contrato = new Contrato();
            contrato.setNombre(nombreUsuario);
            contrato.setEmail(emailUsuario);
            contrato.setTelefono(telefono);
            contrato.setFecha(fechaEvento);
            contrato.setLugar(lugar);
            contrato.setServicio(servicio.getNombre());

            // Combinar descripción del servicio con comentarios adicionales
            String comentariosFinal = servicio.getDescripcion();
            if (comentarios != null && !comentarios.trim().isEmpty()) {
                comentariosFinal += " | Comentarios: " + comentarios;
            }
            contrato.setComentarios(comentariosFinal);

            contratosPendientes.add(contrato);
        }

        // Guardar la lista actualizada de contratos pendientes en el contexto global
        session.getServletContext().setAttribute("contratosPendientes", contratosPendientes);

        // Limpiar el carrito después de procesar
        session.removeAttribute("carrito");

        // Recalcular total a 0
        double totalPagar = 0.0;
        session.setAttribute("totalPagar", String.format("%.2f", totalPagar));

        redirectAttributes.addFlashAttribute("mensaje",
                "¡Contrato(s) enviado(s) exitosamente! Se han enviado " + servicios.size() +
                        " contrato(s) para revisión del administrador.");

        return "redirect:/sesion";
    }

    @GetMapping("/descargar-reporte-excel")
    public String descargarReporteExcel(RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", "Reporte Excel en generación (simulación)...");
        return "redirect:/sesion";
    }
}