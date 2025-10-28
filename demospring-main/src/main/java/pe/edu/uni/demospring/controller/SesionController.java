package pe.edu.uni.demospring.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.model.Servicio;
import java.util.ArrayList;
// Librerías para PDF (OpenPDF)
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

// Librerías para Excel (Apache POI)
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    @GetMapping("/descargar-reporte")
    public void descargarPDF(HttpServletResponse response, HttpSession session) throws IOException, DocumentException {

        List<Servicio> servicios = (List<Servicio>) session.getAttribute("carrito");


        if (servicios == null || servicios.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No hay servicios para exportar");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=servicios.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("📋 Reporte de Servicios Contratados\n\n", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell("Servicio");
        table.addCell("Descripción");
        table.addCell("Precio (S/.)");

        double total = 0;
        for (Servicio s : servicios) {
            table.addCell(s.getNombre());
            table.addCell(s.getDescripcion());
            table.addCell(String.format("%.2f", s.getPrecio()));
            total += s.getPrecio();
        }

        document.add(table);
        document.add(new Paragraph("\nTotal: S/ " + String.format("%.2f", total)));
        document.close();
    }


    @GetMapping("/descargar-reporte-excel")
    public void descargarExcel(HttpServletResponse response, HttpSession session) throws IOException {

        List<Servicio> servicios = (List<Servicio>) session.getAttribute("carrito");


        if (servicios == null || servicios.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No hay servicios para exportar");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=servicios.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Servicios");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Servicio");
        header.createCell(1).setCellValue("Descripción");
        header.createCell(2).setCellValue("Precio (S/.)");

        int rowCount = 1;
        double total = 0;
        for (Servicio s : servicios) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(s.getNombre());
            row.createCell(1).setCellValue(s.getDescripcion());
            row.createCell(2).setCellValue(s.getPrecio());
            total += s.getPrecio();
        }

        Row totalRow = sheet.createRow(rowCount);
        totalRow.createCell(1).setCellValue("Total:");
        totalRow.createCell(2).setCellValue(total);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}