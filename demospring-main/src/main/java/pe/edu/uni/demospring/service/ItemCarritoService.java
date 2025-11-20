package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.ItemCarrito;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.repository.ItemCarritoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemCarritoService {

    private final ItemCarritoRepository itemCarritoRepository;

    @Autowired
    public ItemCarritoService(ItemCarritoRepository itemCarritoRepository) {
        this.itemCarritoRepository = itemCarritoRepository;
    }

    public ItemCarrito guardarItem(ItemCarrito item) {
        return itemCarritoRepository.save(item);
    }

    public List<ItemCarrito> obtenerCarritoPorUsuario(Usuario usuario) {
        return itemCarritoRepository.findByUsuario(usuario);
    }

    public Optional<ItemCarrito> buscarItemExistente(Usuario usuario, Servicio servicio) {
        return itemCarritoRepository.findByUsuarioAndServicio(usuario, servicio);
    }

    public void eliminarItem(ItemCarrito item) {
        itemCarritoRepository.delete(item);
    }

    /**
     * Elimina todos los ítems del carrito de un usuario.
     * Esta operación es transaccional.
     */
    @Transactional
    public void vaciarCarrito(Usuario usuario) {
        itemCarritoRepository.deleteByUsuario(usuario);
    }
}