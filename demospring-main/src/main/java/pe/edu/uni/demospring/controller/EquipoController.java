package pe.edu.uni.demospring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.demospring.model.Equipo;
import pe.edu.uni.demospring.service.EquipoService;

import java.util.List;

@Controller
@RequestMapping("/admin/gestionequipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping
    public String listarEquipos(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        List<Equipo> equipos = (buscar != null && !buscar.isEmpty())
                ? equipoService.buscar(buscar)
                : equipoService.listarTodos();
        model.addAttribute("equipos", equipos);
        model.addAttribute("buscar", buscar);
        return "gestionEquipos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("equipo", new Equipo());
        return "formEquipo";
    }

    @PostMapping("/guardar")
    public String guardarEquipo(@ModelAttribute Equipo equipo) {
        equipoService.guardar(equipo);
        return "redirect:/admin/gestionequipos";
    }

    @GetMapping("/editar/{id}")
    public String editarEquipo(@PathVariable Long id, Model model) {
        Equipo equipo = equipoService.obtenerPorId(id);
        model.addAttribute("equipo", equipo);
        return "formEquipo";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id) {
        equipoService.eliminar(id);
        return "redirect:/admin/gestionequipos";
    }
}
