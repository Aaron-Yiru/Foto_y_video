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
import pe.edu.uni.demospring.repository.ServicioRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Controller
@RequestMapping("/sesion")
public class SesionController {

    @Autowired
    private ServicioRepository servicioRepository;

    @GetMapping
    public String mostrarSesion(Model model, HttpSession session) {
        Boolean logueado = (Boolean) session.getAttribute("usuarioLogueado");
        if (logueado == null || !logueado) {
            return "redirect:/perfil";
        }

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

        return "sesion";
    }

    @PostMapping("/agregar-servicio")
    public String agregarServicio(@RequestParam("servicioId") Long servicioId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        Boolean logueado = (Boolean) session.getAttribute("usuarioLogueado");
        if (logueado == null || !logueado) {
            ra.addFlashAttribute("error", "Necesitas iniciar sesión para contratar servicios.");
            return "redirect:/perfil";
        }

        Servicio servicio = servicioRepository.findById(servicioId).orElse(null);

        if (servicio == null) {
            ra.addFlashAttribute("error", "El servicio seleccionado no existe o fue eliminado.");
            return "redirect:/servicios";
        }

        @SuppressWarnings("unchecked")
        List<Servicio> carrito = (List<Servicio>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        boolean yaExiste = carrito.stream()
                .anyMatch(s -> s.getId().equals(servicioId));

        if (yaExiste) {
            ra.addFlashAttribute("error", "El servicio '" + servicio.getNombre() + "' ya está en tu carrito.");
        } else {
            carrito.add(servicio);
            session.setAttribute("carrito", carrito);
            ra.addFlashAttribute("mensaje", "Servicio '" + servicio.getNombre() + "' añadido al carrito.");
        }

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

        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (nombreUsuario == null || emailUsuario == null) {
            redirectAttributes.addFlashAttribute("error", "Sesión expirada. Por favor inicia sesión nuevamente.");
            return "redirect:/perfil";
        }

        @SuppressWarnings("unchecked")
        List<Servicio> servicios = (List<Servicio>) session.getAttribute("carrito");

        if (servicios == null || servicios.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No hay servicios en el carrito");
            return "redirect:/sesion";
        }

        @SuppressWarnings("unchecked")
        List<Contrato> contratosPendientes = (List<Contrato>) session.getServletContext()
                .getAttribute("contratosPendientes");
        if (contratosPendientes == null) {
            contratosPendientes = new ArrayList<>();
        }

        for (Servicio servicio : servicios) {
            Contrato contrato = new Contrato();
            contrato.setNombre(nombreUsuario);
            contrato.setEmail(emailUsuario);
            contrato.setTelefono(telefono);
            contrato.setFecha(fechaEvento);
            contrato.setLugar(lugar);
            contrato.setServicio(servicio.getNombre());

            String comentariosFinal = servicio.getDescripcion();
            if (comentarios != null && !comentarios.trim().isEmpty()) {
                comentariosFinal += " | Comentarios: " + comentarios;
            }
            contrato.setComentarios(comentariosFinal);

            contratosPendientes.add(contrato);
        }

        session.getServletContext().setAttribute("contratosPendientes", contratosPendientes);
        session.removeAttribute("carrito");
        session.setAttribute("totalPagar", String.format("%.2f", 0.0));

        redirectAttributes.addFlashAttribute("mensaje",
                "¡Contrato(s) enviado(s) exitosamente! Se han enviado " + servicios.size() +
                        " contrato(s) para revisión del administrador.");

        return "redirect:/sesion";
    }

    @GetMapping("/descargar-reporte")
    public void descargarPDF(HttpServletResponse response, HttpSession session) throws IOException, DocumentException {
        @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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
