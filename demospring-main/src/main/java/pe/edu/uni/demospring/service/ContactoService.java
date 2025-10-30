package pe.edu.uni.demospring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.uni.demospring.model.Contacto;
import pe.edu.uni.demospring.repository.ContactoRepository;

import java.util.List;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    public void guardarMensaje(Contacto contacto) {
        contactoRepository.save(contacto);
    }

    public List<Contacto> listarMensajes() {
        return contactoRepository.findAll();
    }
}
