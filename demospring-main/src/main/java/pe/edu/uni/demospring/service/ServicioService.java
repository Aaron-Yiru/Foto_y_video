package pe.edu.uni.demospring.service;

import org.springframework.stereotype.Service;
import pe.edu.uni.demospring.model.Servicio;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioService {

    private final List<Servicio> listaServicios = new ArrayList<>();

    public List<Servicio> listar() {
        return listaServicios;
    }

    public void agregar(Servicio servicio) {
        servicio.setId((long) (listaServicios.size() + 1));
        listaServicios.add(servicio);
    }

    public void eliminar(Long id) {
        listaServicios.removeIf(s -> s.getId().equals(id));
    }
}
