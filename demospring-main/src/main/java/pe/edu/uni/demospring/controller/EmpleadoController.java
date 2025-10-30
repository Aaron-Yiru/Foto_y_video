package pe.edu.uni.demospring.controller;

import pe.edu.uni.demospring.model.Empleado;
import pe.edu.uni.demospring.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/gestionempleados")
    public String listarEmpleados(Model model, @RequestParam(required = false) String q) {
        List<Empleado> empleados = (q == null || q.isEmpty())
                ? empleadoService.listarTodos()
                : empleadoService.buscar(q);

        model.addAttribute("empleados", empleados);
        model.addAttribute("busqueda", q);
        return "gestionEmpleados";
    }

    @GetMapping("/nuevoempleado")
    public String nuevoEmpleadoForm(Model model) {
        model.addAttribute("empleado", new Empleado());
        return "formEmpleado";
    }

    @PostMapping("/guardarEmpleado")
    public String guardarEmpleado(@ModelAttribute Empleado empleado) {
        empleadoService.guardar(empleado);
        return "redirect:/admin/gestionempleados"; // ✅ corregido
    }

    @GetMapping("/editarEmpleado/{id}")
    public String editarEmpleado(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoService.obtenerPorId(id);
        model.addAttribute("empleado", empleado);
        return "formEmpleado";
    }

    @GetMapping("/eliminarEmpleado/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return "redirect:/admin/gestionempleados"; // ✅ corregido
    }
}
