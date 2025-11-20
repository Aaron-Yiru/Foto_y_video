package pe.edu.uni.demospring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uni.demospring.model.ItemCarrito;
import pe.edu.uni.demospring.model.Servicio;
import pe.edu.uni.demospring.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByUsuario(Usuario usuario);

    Optional<ItemCarrito> findByUsuarioAndServicio(Usuario usuario, Servicio servicio);

    void deleteByUsuario(Usuario usuario);
}