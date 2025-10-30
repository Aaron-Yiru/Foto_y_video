package pe.edu.uni.demospring.controller;

import jakarta.servlet.http.HttpServletResponse;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.service.UsuarioService;
import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.service.ServicioService;
import pe.edu.uni.demospring.service.ContratoService;

// ⭐ NUEVAS IMPORTACIONES PARA PERSISTENCIA DEL CARRITO
import pe.edu.uni.demospring.model.ItemCarrito;
import pe.edu.uni.demospring.repository.ItemCarritoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

// Librerías para PDF y Excel (Mantenidas)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final ServicioService servicioService;
    private final ContratoService contratoService;

    // ⭐ DEPENDENCIA DEL REPOSITORIO DE CARRITO (PERSISTENTE)
    private final ItemCarritoRepository itemCarritoRepository;


    @Autowired
    public PerfilController(UsuarioService usuarioService, ServicioService servicioService, ContratoService contratoService, ItemCarritoRepository itemCarritoRepository) {
        this.usuarioService = usuarioService;
        this.servicioService = servicioService;
        this.contratoService = contratoService;
        this.itemCarritoRepository = itemCarritoRepository; // INYECCIÓN
    }

    // --- 1. VISTA ÚNICA PERFIL/SESIÓN: CARGA DESDE LA DB ---
    @GetMapping
    public String mostrarPerfilSesion(Model model, HttpSession session) {
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario != null) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // ⭐ Carga desde la DB: Obtener ItemsCarrito del usuario
                List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);

                // Convertir ItemsCarrito a lista de Servicios para la vista
                List<Servicio> carrito = itemsCarrito.stream()
                        .map(ItemCarrito::getServicio)
                        .collect(Collectors.toList());

                double totalPagar = carrito.stream().mapToDouble(Servicio::getPrecio).sum();

                // Pasamos los datos del carrito persistente a la vista (clave "servicios")
                model.addAttribute("servicios", carrito);
                model.addAttribute("totalPagar", String.format("%.2f", totalPagar));

                // También guardamos la lista en la sesión para uso en reportes (PDF/Excel)
                session.setAttribute("carrito", carrito);

                return "perfil-sesion";
            }
        }

        // Si no está logueado, muestra la vista para login/registro (que es la misma plantilla)
        return "perfil-sesion";
    }

    // --- 2. AUTENTICACIÓN ---
    @PostMapping("/registrar")
    public String registrarUsuario(
            @RequestParam String nombre, @RequestParam String email,
            @RequestParam String password, @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "perfil-sesion";
        }
        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "Este correo electrónico ya está registrado.");
            return "perfil-sesion";
        }

        Usuario nuevoUsuario = new Usuario(nombre, email, password);
        usuarioService.registrarUsuario(nuevoUsuario);

        model.addAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.");
        return "perfil-sesion";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam String email, @RequestParam String password,
            HttpSession session, RedirectAttributes ra) {

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(password)) {
            session.setAttribute("usuarioLogueado", true);
            session.setAttribute("nombreUsuario", usuarioOpt.get().getNombre());
            session.setAttribute("emailUsuario", usuarioOpt.get().getEmail());
            // El carrito se carga automáticamente al redireccionar a /perfil (el @GetMapping anterior)

            return "redirect:/perfil";
        }

        ra.addFlashAttribute("error", "Correo o contraseña incorrectos.");
        return "redirect:/perfil";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        // La sesión se invalida, pero el carrito P E R M A N E C E en la DB.
        session.invalidate();
        return "redirect:/perfil";
    }

    // --- 3. CARRITO / CONTRATACIÓN ---
    @PostMapping("/agregar-servicio")
    public String agregarServicio(@RequestParam("servicioId") Long servicioId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        // 1. Verificar autenticación
        if (emailUsuario == null) {
            ra.addFlashAttribute("error", "Necesitas iniciar sesión para contratar servicios.");
            return "redirect:/perfil";
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);
        Servicio servicio = servicioService.buscarPorId(servicioId);

        if (usuarioOpt.isEmpty() || servicio == null) {
            ra.addFlashAttribute("error", "Error de usuario o servicio no válido.");
            return "redirect:/servicios";
        }
        Usuario usuario = usuarioOpt.get();

        // 2. ⭐ REVISA LA DB ANTES DE AÑADIR (Persistencia)
        Optional<ItemCarrito> existingItem = itemCarritoRepository.findByUsuarioAndServicio(usuario, servicio);

        if (existingItem.isPresent()) {
            ra.addFlashAttribute("error", "El servicio '" + servicio.getNombre() + "' ya está en tu carrito.");
        } else {
            // 3. ⭐ GUARDA EN LA DB
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setUsuario(usuario);
            nuevoItem.setServicio(servicio);
            itemCarritoRepository.save(nuevoItem);

            ra.addFlashAttribute("mensaje", "Servicio '" + servicio.getNombre() + "' añadido al carrito.");
        }

        return "redirect:/servicios";
    }


    @PostMapping("/eliminar-servicio")
    // Necesitas el ID del servicio que se va a eliminar
    public String eliminarServicio(@RequestParam("servicioId") Long servicioId, HttpSession session, RedirectAttributes ra) {
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario == null) {
            ra.addFlashAttribute("error", "Sesión inválida.");
            return "redirect:/perfil";
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);
        Servicio servicio = servicioService.buscarPorId(servicioId);

        if (usuarioOpt.isPresent() && servicio != null) {
            Usuario usuario = usuarioOpt.get();
            // Busca el ItemCarrito específico para ese usuario y ese servicio
            Optional<ItemCarrito> itemOpt = itemCarritoRepository.findByUsuarioAndServicio(usuario, servicio);

            if (itemOpt.isPresent()) {
                itemCarritoRepository.delete(itemOpt.get()); // Eliminar de la DB
                ra.addFlashAttribute("mensaje", "Servicio '" + servicio.getNombre() + "' eliminado del carrito.");
            }
        }

        return "redirect:/perfil";
    }

    @PostMapping("/procesar-pago")
    @Transactional // ⭐ NECESARIO para que itemCarritoRepository.deleteByUsuario funcione correctamente
    public String procesarPagoYContratar(
            @RequestParam String telefono,
            @RequestParam String fechaEvento,
            @RequestParam String lugar,
            @RequestParam(required = false) String comentarios,
            @RequestParam(required = false) String horaEvento,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario == null) {
            redirectAttributes.addFlashAttribute("error", "Error: Sesión expirada. Por favor, inicia sesión de nuevo.");
            return "redirect:/perfil";
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Error: Usuario no encontrado.");
            return "redirect:/perfil";
        }
        Usuario usuario = usuarioOpt.get();

        // ⭐ CARGAR CARRITO PERSISTENTE DESDE LA DB (Para obtener la lista de servicios contratados)
        List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);
        List<Servicio> serviciosCarrito = itemsCarrito.stream()
                .map(ItemCarrito::getServicio)
                .collect(Collectors.toList());

        // Validación crítica (carrito vacío)
        if (serviciosCarrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Error: El carrito está vacío. Añade servicios para contratar.");
            return "redirect:/perfil";
        }

        // 1. Construir el objeto Contrato
        Contrato nuevoContrato = new Contrato();
        nuevoContrato.setNombre(nombreUsuario);
        nuevoContrato.setEmail(emailUsuario);
        nuevoContrato.setTelefono(telefono);
        nuevoContrato.setFecha(fechaEvento);
        nuevoContrato.setLugar(lugar);
        nuevoContrato.setComentarios(comentarios != null ? comentarios : "");
        nuevoContrato.setHora(horaEvento != null ? horaEvento : "");

        // 2. Concatenar los nombres de los servicios
        String serviciosContratados = serviciosCarrito.stream()
                .map(Servicio::getNombre)
                .collect(Collectors.joining(", "));
        nuevoContrato.setServicio(serviciosContratados);

        // 3. Guardar en la Base de Datos y limpiar el carrito persistente
        try {
            contratoService.guardarContrato(nuevoContrato);

            // ⭐ LIMPIAR CARRITO PERSISTENTE DE LA DB
            itemCarritoRepository.deleteByUsuario(usuario);

            // Limpiar claves de sesión residuales (ya no importantes)
            session.removeAttribute("carrito");
            session.removeAttribute("totalPagar");

            redirectAttributes.addFlashAttribute("mensaje",
                    "✅ ¡Contrato Guardado! Carrito vaciado.");
            return "redirect:/perfil";

        } catch (Exception e) {
            System.err.println("Error al guardar contrato: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "❌ Error de sistema: No se pudo guardar la contratación.");
            return "redirect:/perfil";
        }
    }


    // --- 4. REPORTES (Usan la lista temporalmente guardada en la sesión) ---
    // (Estos métodos asumen que la lista "carrito" fue cargada en el @GetMapping("/perfil"))

    @GetMapping("/descargar-reporte-pdf")
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
