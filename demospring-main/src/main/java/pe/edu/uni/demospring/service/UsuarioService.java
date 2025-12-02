package pe.edu.uni.demospring.service;

import pe.edu.uni.demospring.model.Rol;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.repository.RolRepository;
import pe.edu.uni.demospring.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario con seguridad:
     * 1. Hashea la contraseña.
     * 2. Asigna el rol por defecto (ROLE_USER).
     */
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // 1. Encriptar contraseña (¡CRÍTICO!)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 2. Asignar Rol por defecto (ROLE_USER)
        // Si el rol no existe en la BD (primera vez), lo crea al vuelo para evitar errores.
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> rolRepository.save(new Rol("ROLE_USER")));

        usuario.agregarRol(rolUser);

        return usuarioRepository.save(usuario);
    }

    // --- Métodos de lectura (sin cambios mayores) ---

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    // --- Métodos de escritura ---

    public Usuario guardar(Usuario usuario) {
        // Nota: Si usas este método para editar, asegúrate de no re-encriptar
        // una contraseña que ya está encriptada.
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    // --- Conteos ---

    public long contarUsuarios() { return usuarioRepository.count(); }
    public long contarUsuariosActivos() { return usuarioRepository.countByActivoTrue(); }
    public long contarUsuariosInactivos() { return usuarioRepository.countByActivoFalse(); }
    public long contarTodos() { return usuarioRepository.count(); }

    public long contarPorEstado(boolean activo) {
        return activo ? usuarioRepository.countByActivoTrue() : usuarioRepository.countByActivoFalse();
    }
}