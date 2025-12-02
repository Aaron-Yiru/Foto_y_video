package pe.edu.uni.demospring.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Modelos
import pe.edu.uni.demospring.model.Contrato;
import pe.edu.uni.demospring.model.ItemCarrito;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.security.MyUserPrincipal;

// Repositorios y Servicios
import pe.edu.uni.demospring.repository.ItemCarritoRepository;
import pe.edu.uni.demospring.repository.ContratoRepository; // ✅ Importante para el historial
import pe.edu.uni.demospring.service.ContratoService;
import pe.edu.uni.demospring.service.ServicioService;
import pe.edu.uni.demospring.service.UsuarioService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final ServicioService servicioService;
    private final ContratoService contratoService;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ContratoRepository contratoRepository; // ✅ Nuevo repositorio inyectado

    @Autowired
    public PerfilController(UsuarioService usuarioService,
                            ServicioService servicioService,
                            ContratoService contratoService,
                            ItemCarritoRepository itemCarritoRepository,
                            ContratoRepository contratoRepository) {
        this.usuarioService = usuarioService;
        this.servicioService = servicioService;
        this.contratoService = contratoService;
        this.itemCarritoRepository = itemCarritoRepository;
        this.contratoRepository = contratoRepository;
    }

    // ------------------------------------------------------------------------
    // 1. VISTA PRINCIPAL (LOGIN / PERFIL / HISTORIAL)
    // ------------------------------------------------------------------------
    @GetMapping
    public String mostrarPerfilSesion(Model model, @AuthenticationPrincipal MyUserPrincipal principal) {

        // Si el usuario NO está logueado, mostramos el formulario de login/registro
        if (principal == null) {
            return "perfil-sesion";
        }

        // Si SÍ está logueado, cargamos sus datos
        Usuario usuario = usuarioService.obtenerPorEmail(principal.getUsername());

        // 1. Cargar Carrito desde DB
        List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);
        List<Servicio> carrito = itemsCarrito.stream()
                .map(ItemCarrito::getServicio)
                .collect(Collectors.toList());

        double totalPagar = carrito.stream().mapToDouble(Servicio::getPrecio).sum();

        // 2. ✅ CARGAR HISTORIAL DE CONTRATOS
        // Esto busca los contratos usando el email del usuario y los ordena por el más reciente
        List<Contrato> misContratos = contratoRepository.findByEmailOrderByIdDesc(usuario.getEmail());

        // 3. Enviar todo a la vista
        model.addAttribute("nombreUsuario", usuario.getNombre());
        model.addAttribute("emailUsuario", usuario.getEmail());
        model.addAttribute("servicios", carrito);
        model.addAttribute("totalPagar", String.format("%.2f", totalPagar));
        model.addAttribute("misContratos", misContratos); // ✅ Enviamos la lista al HTML

        return "perfil-sesion";
    }

    // ------------------------------------------------------------------------
    // 2. REGISTRO DE USUARIO
    // ------------------------------------------------------------------------
    @PostMapping("/registrar")
    public String registrarUsuario(
            @RequestParam String nombre, @RequestParam String email,
            @RequestParam String password, @RequestParam String confirmPassword,
            RedirectAttributes ra) {

        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/perfil";
        }
        if (usuarioService.existeEmail(email)) {
            ra.addFlashAttribute("error", "Este correo electrónico ya está registrado.");
            return "redirect:/perfil";
        }

        // El servicio encripta la contraseña y asigna ROLE_USER automáticamente
        Usuario nuevoUsuario = new Usuario(nombre, email, password);
        usuarioService.registrarUsuario(nuevoUsuario);

        ra.addFlashAttribute("mensaje", "¡Registro exitoso! Por favor inicia sesión.");
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------------------
    // 3. GESTIÓN DEL CARRITO
    // ------------------------------------------------------------------------
    @PostMapping("/agregar-servicio")
    public String agregarServicio(@RequestParam("servicioId") Long servicioId,
                                  @AuthenticationPrincipal MyUserPrincipal principal,
                                  RedirectAttributes ra) {

        if (principal == null) {
            return "redirect:/perfil";
        }

        Usuario usuario = usuarioService.obtenerPorEmail(principal.getUsername());
        Servicio servicio = servicioService.buscarPorId(servicioId);

        if (servicio != null) {
            if (itemCarritoRepository.findByUsuarioAndServicio(usuario, servicio).isPresent()) {
                ra.addFlashAttribute("error", "El servicio '" + servicio.getNombre() + "' ya está en tu carrito.");
            } else {
                ItemCarrito nuevoItem = new ItemCarrito();
                nuevoItem.setUsuario(usuario);
                nuevoItem.setServicio(servicio);
                itemCarritoRepository.save(nuevoItem);
                ra.addFlashAttribute("mensaje", "Servicio agregado al carrito.");
            }
        }
        return "redirect:/servicios";
    }

    @PostMapping("/eliminar-servicio")
    public String eliminarServicio(@RequestParam("servicioIndex") int index,
                                   @AuthenticationPrincipal MyUserPrincipal principal,
                                   RedirectAttributes ra) {
        if (principal != null) {
            Usuario usuario = usuarioService.obtenerPorEmail(principal.getUsername());
            List<ItemCarrito> items = itemCarritoRepository.findByUsuario(usuario);

            if (index >= 0 && index < items.size()) {
                itemCarritoRepository.delete(items.get(index));
                ra.addFlashAttribute("mensaje", "Servicio eliminado.");
            }
        }
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------------------
    // 4. PROCESAR PAGO Y GENERAR CONTRATO
    // ------------------------------------------------------------------------
    @PostMapping("/procesar-pago")
    @Transactional
    public String procesarPagoYContratar(
            @RequestParam String telefono, @RequestParam String fechaEvento,
            @RequestParam String lugar, @RequestParam(required = false) String comentarios,
            @RequestParam(required = false) String horaEvento,
            @AuthenticationPrincipal MyUserPrincipal principal,
            RedirectAttributes ra) {

        if (principal == null) return "redirect:/perfil";

        Usuario usuario = usuarioService.obtenerPorEmail(principal.getUsername());
        List<ItemCarrito> itemsCarrito = itemCarritoRepository.findByUsuario(usuario);

        if (itemsCarrito.isEmpty()) {
            ra.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/perfil";
        }

        // Crear Contrato
        Contrato contrato = new Contrato();
        contrato.setNombre(usuario.getNombre());
        contrato.setEmail(usuario.getEmail());
        contrato.setTelefono(telefono);
        contrato.setFecha(fechaEvento);
        contrato.setLugar(lugar);
        contrato.setComentarios(comentarios != null ? comentarios : "");
        contrato.setHora(horaEvento != null ? horaEvento : "");

        String serviciosStr = itemsCarrito.stream()
                .map(i -> i.getServicio().getNombre())
                .collect(Collectors.joining(", "));
        contrato.setServicio(serviciosStr);

        // Guardar contrato en la BD
        contratoService.guardarContrato(contrato);

        // Vaciar el carrito
        itemCarritoRepository.deleteByUsuario(usuario);

        ra.addFlashAttribute("mensaje", "✅ ¡Contratación exitosa! Tu pedido ha sido registrado en el historial.");

        // Al redireccionar a /perfil, el método mostrarPerfilSesion volverá a ejecutarse
        // y cargará la lista actualizada de contratos en la tabla 'misContratos'.
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------------------
    // 5. REPORTES (PDF / EXCEL)
    // ------------------------------------------------------------------------
    @GetMapping("/descargar-reporte-pdf")
    public void descargarPDF(HttpServletResponse response, @AuthenticationPrincipal MyUserPrincipal principal)
            throws IOException, DocumentException {

        List<Servicio> servicios = obtenerServiciosUsuario(principal);
        if (servicios.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No hay datos para exportar");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=servicios.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("📋 Reporte de Servicios\n\n", tituloFont);
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
        document.add(new Paragraph("\nTotal Estimado: S/ " + String.format("%.2f", total)));
        document.close();
    }

    @GetMapping("/descargar-reporte-excel")
    public void descargarExcel(HttpServletResponse response, @AuthenticationPrincipal MyUserPrincipal principal)
            throws IOException {

        List<Servicio> servicios = obtenerServiciosUsuario(principal);
        if (servicios.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No hay datos");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=servicios.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Servicios");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Servicio");
        header.createCell(1).setCellValue("Descripción");
        header.createCell(2).setCellValue("Precio");

        int rowNum = 1;
        for (Servicio s : servicios) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(s.getNombre());
            row.createCell(1).setCellValue(s.getDescripcion());
            row.createCell(2).setCellValue(s.getPrecio());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Helper para no repetir código en reportes
    private List<Servicio> obtenerServiciosUsuario(MyUserPrincipal principal) {
        if (principal == null) return Collections.emptyList();
        Usuario usuario = usuarioService.obtenerPorEmail(principal.getUsername());
        return itemCarritoRepository.findByUsuario(usuario).stream()
                .map(ItemCarrito::getServicio)
                .collect(Collectors.toList());
    }
}