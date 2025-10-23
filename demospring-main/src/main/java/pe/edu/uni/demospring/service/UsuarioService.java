package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     * NOTA: En un proyecto real, la contraseña DEBE ser hasheada aquí.
     * @param usuario El objeto Usuario a registrar.
     * @return El Usuario guardado.
     */
    public Usuario registrarUsuario(Usuario usuario) {
        // Lógica de negocio (ej. validar datos, hashear contraseña)
        return usuarioRepository.save(usuario);
    }

    /**
     * Busca un usuario por su email para el proceso de inicio de sesión.
     * @param email Correo del usuario.
     * @return Optional que puede contener el Usuario.
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Verifica si un email ya está registrado.
     * @param email Correo a verificar.
     * @return true si ya existe, false si está disponible.
     */
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Obtiene todos los usuarios.
     * @return Lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id ID del usuario.
     * @return El usuario si existe, null en caso contrario.
     */
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene un usuario por su email.
     * @param email Email del usuario.
     * @return El usuario encontrado.
     */
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    /**
     * Guarda un nuevo usuario.
     * @param usuario El usuario a guardar.
     * @return El usuario guardado.
     */
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente.
     * @param usuario El usuario a actualizar.
     * @return El usuario actualizado.
     */
    public Usuario actualizar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario por su ID.
     * @param id ID del usuario a eliminar.
     */
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Cuenta el total de usuarios.
     * @return Número total de usuarios.
     */
    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    /**
     * Cuenta los usuarios activos.
     * @return Número de usuarios activos.
     */
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    /**
     * Cuenta los usuarios inactivos.
     * @return Número de usuarios inactivos.
     */
    public long contarUsuariosInactivos() {
        return usuarioRepository.countByActivoFalse();
    }

    public long contarTodos() {
        return usuarioRepository.count();
    }
    /**
     * Cuenta los usuarios según su estado (activo o inactivo).
     * @param activo true para contar usuarios activos, false para inactivos.
     * @return Número de usuarios con el estado indicado.
     */
    public long contarPorEstado(boolean activo) {
        return activo ? usuarioRepository.countByActivoTrue() : usuarioRepository.countByActivoFalse();
    }


}