package pe.edu.uni.demospring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.demospring.model.Contacto;
import pe.edu.uni.demospring.repository.ContactoRepository;

@Controller
@RequestMapping("/contacto-form")
public class ContactoController {

    @Autowired
    private ContactoRepository contactoRepository;

    // Mostrar página de contacto
    @GetMapping
    public String mostrarFormulario() {
        return "contacto";
    }

    // Guardar mensaje enviado
    @PostMapping("/guardar")
    public String guardarMensaje(@RequestParam String nombre,
                                 @RequestParam String email,
                                 @RequestParam String asunto,
                                 @RequestParam String mensaje,
                                 Model model) {

        // Crear objeto y guardar en la base de datos
        Contacto nuevoContacto = new Contacto(nombre, email, asunto, mensaje);
        contactoRepository.save(nuevoContacto);

        // Mostrar mensaje de éxito en la vista
        model.addAttribute("mensajeExito", "✅ Tu mensaje fue enviado correctamente.");
        return "contacto";
    }
}
