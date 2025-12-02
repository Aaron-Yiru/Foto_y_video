package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.service.ContratoService;
import pe.edu.uni.demospring.model.Contrato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ContratoController {

    private final ContratoService contratoService;

    @Autowired
    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    // VISTA ADMIN: Ver contratos guardados en la BD
    // La seguridad está en WebSecurityConfig: .requestMatchers("/verContratos").hasRole("ADMIN")
    @GetMapping("/verContratos")
    public String verContratos(Model model) {

        // 1. Obtener todos los contratos de la DB
        List<Contrato> contratos = contratoService.obtenerTodos();

        // 2. Pasar datos al modelo
        model.addAttribute("contratos", contratos);
        model.addAttribute("totalContratos", contratos.size());

        return "lista-contratos";
    }
}