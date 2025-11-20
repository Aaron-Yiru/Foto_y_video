package pe.edu.uni.demospring.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Modelos
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.model.ItemCarrito;

// Servicios y Repositorios
import pe.edu.uni.demospring.service.UsuarioService;
import pe.edu.uni.demospring.service.ServicioService;
import pe.edu.uni.demospring.service.ContratoService;
import pe.edu.uni.demospring.repository.ItemCarritoRepository;

// Librerías para PDF (OpenPDF / iText)
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final ServicioService servicioService;
    private final ContratoService contratoService;
    private final ItemCarritoRepository itemCarritoRepository;

    @Autowired
    public PerfilController(UsuarioService usuarioService,
                            ServicioService servicioService,
                            ContratoService contratoService,
                            ItemCarritoRepository itemCarritoRepository) {
        this.usuarioService = usuarioService;
        this.servicioService = servicioService;
        this.contratoService = contratoService;
        this.itemCarritoRepository = itemCarritoRepository;
    }

    // ------------------------------------------------------------------------
    // 1. VISTA PRINCIPAL (LOGIN / PERFIL) - CON LA CORRECCIÓN
    // ------------------------------------------------------------------------
    @GetMapping
    public String mostrarPerfilSesion(Model model, HttpSession session) {
        // Recuperamos el email guardado en la sesión al hacer login
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario != null) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // ⭐ CORRECCIÓN APLICADA AQUÍ ABAJO ⭐
                // Pasamos los datos al Model para que el th:if="${nombreUsuario != null}" funcione
                model.addAttribute("nombreUsuario", usuario.getNombre());
                model.addAttribute("emailUsuario", usuario.getEmail());

                // Carga desde la DB: Obtener ItemsCarrito del usuario
                List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);

                // Convertir ItemsCarrito a lista de Servicios para la vista
                List<Servicio> carrito = itemsCarrito.stream()
                        .map(ItemCarrito::getServicio)
                        .collect(Collectors.toList());

                double totalPagar = carrito.stream().mapToDouble(Servicio::getPrecio).sum();

                // Pasamos los datos del carrito a la vista
                model.addAttribute("servicios", carrito);
                model.addAttribute("totalPagar", String.format("%.2f", totalPagar));

                // Guardamos en sesión para los reportes PDF/Excel
                session.setAttribute("carrito", carrito);

                // IMPORTANTE: Asegúrate de que tu archivo HTML se llame 'perfil-sesion.html'
                // Si se llama 'perfil.html', cambia esto a return "perfil";
                return "perfil-sesion";
            }
        }

        // Si no hay usuario logueado, se muestra la misma vista (pero se verán los formularios de login)
        return "perfil-sesion";
    }

    // ------------------------------------------------------------------------
    // 2. AUTENTICACIÓN (LOGIN / REGISTRO / LOGOUT)
    // ------------------------------------------------------------------------

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
            // Guardamos datos clave en la sesión
            session.setAttribute("usuarioLogueado", true);
            session.setAttribute("nombreUsuario", usuarioOpt.get().getNombre());
            session.setAttribute("emailUsuario", usuarioOpt.get().getEmail());

            // Redireccionamos al @GetMapping, que ahora cargará correctamente los datos
            return "redirect:/perfil";
        }

        ra.addFlashAttribute("error", "Correo o contraseña incorrectos.");
        return "redirect:/perfil";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); // Destruye la sesión
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------------------
    // 3. GESTIÓN DEL CARRITO (CON BASE DE DATOS)
    // ------------------------------------------------------------------------

    @PostMapping("/agregar-servicio")
    public String agregarServicio(@RequestParam("servicioId") Long servicioId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        // Verificar autenticación
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

        // Revisar si ya existe en la DB
        Optional<ItemCarrito> existingItem = itemCarritoRepository.findByUsuarioAndServicio(usuario, servicio);

        if (existingItem.isPresent()) {
            ra.addFlashAttribute("error", "El servicio '" + servicio.getNombre() + "' ya está en tu carrito.");
        } else {
            // Guardar en la DB
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setUsuario(usuario);
            nuevoItem.setServicio(servicio);
            itemCarritoRepository.save(nuevoItem);

            ra.addFlashAttribute("mensaje", "Servicio '" + servicio.getNombre() + "' añadido al carrito.");
        }

        return "redirect:/servicios";
    }


    @PostMapping("/eliminar-servicio")
    public String eliminarServicio(@RequestParam("servicioIndex") int index, // Mantenemos el index por compatibilidad si usas lista
                                   HttpSession session, RedirectAttributes ra) {
        // NOTA: Tu HTML enviaba "servicioIndex". Idealmente deberías enviar el ID del servicio.
        // Para simplificar y mantener tu lógica de DB, intentaré obtener la lista y borrar por posición,
        // o idealmente deberías cambiar el HTML para enviar th:value="${servicio.id}" con name="servicioId".

        // Aquí asumo que en el futuro cambiarás a ID, pero si usas index con DB es peligroso.
        // Voy a asumir que recuperamos la lista y borramos el elemento correspondiente.

        String emailUsuario = (String) session.getAttribute("emailUsuario");
        if (emailUsuario == null) return "redirect:/perfil";

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            List<ItemCarrito> items = itemCarritoRepository.findByUsuario(usuario);

            if (index >= 0 && index < items.size()) {
                ItemCarrito itemABorrar = items.get(index);
                itemCarritoRepository.delete(itemABorrar);
                ra.addFlashAttribute("mensaje", "Servicio eliminado del carrito.");
            }
        }
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------------------
    // 4. PROCESAR PAGO Y CONTRATAR
    // ------------------------------------------------------------------------
    @PostMapping("/procesar-pago")
    @Transactional
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
            redirectAttributes.addFlashAttribute("error", "Sesión expirada.");
            return "redirect:/perfil";
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(emailUsuario);
        if (usuarioOpt.isEmpty()) return "redirect:/perfil";
        Usuario usuario = usuarioOpt.get();

        // Cargar carrito desde DB
        List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);
        List<Servicio> serviciosCarrito = itemsCarrito.stream()
                .map(ItemCarrito::getServicio)
                .collect(Collectors.toList());

        if (serviciosCarrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/perfil";
        }

        // Crear Contrato
        Contrato nuevoContrato = new Contrato();
        nuevoContrato.setNombre(nombreUsuario);
        nuevoContrato.setEmail(emailUsuario);
        nuevoContrato.setTelefono(telefono);
        nuevoContrato.setFecha(fechaEvento);
        nuevoContrato.setLugar(lugar);
        nuevoContrato.setComentarios(comentarios != null ? comentarios : "");
        nuevoContrato.setHora(horaEvento != null ? horaEvento : "");

        String serviciosContratados = serviciosCarrito.stream()
                .map(Servicio::getNombre)
                .collect(Collectors.joining(", "));
        nuevoContrato.setServicio(serviciosContratados);

        try {
            contratoService.guardarContrato(nuevoContrato);

            // Limpiar DB y Sesión
            itemCarritoRepository.deleteByUsuario(usuario);
            session.removeAttribute("carrito");
            session.removeAttribute("totalPagar");

            redirectAttributes.addFlashAttribute("mensaje", "✅ ¡Contrato Exitoso! Nos pondremos en contacto.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al guardar la contratación.");
        }

        return "redirect:/perfil";
    }


    // ------------------------------------------------------------------------
    // 5. REPORTES (PDF / EXCEL)
    // ------------------------------------------------------------------------

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