package pe.edu.uni.demospring.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.uni.demospring.model.Rol;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.repository.RolRepository;
import pe.edu.uni.demospring.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
                                      RolRepository rolRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Crear Roles si no existen
            crearRolSiNoExiste(rolRepository, "ROLE_ADMIN");
            crearRolSiNoExiste(rolRepository, "ROLE_USER");

            // 2. Crear Usuario ADMIN si no existe
            if (usuarioRepository.findByEmail("admin@uni.edu.pe").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador Principal");
                admin.setEmail("admin@uni.edu.pe");
                admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña encriptada
                admin.setActivo(true);

                // Asignar rol de ADMIN
                Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").get();
                admin.agregarRol(rolAdmin);

                usuarioRepository.save(admin);
                System.out.println(" USUARIO ADMIN CREADO: admin@uni.edu.pe / admin123");
            }
        };
    }

    private void crearRolSiNoExiste(RolRepository rolRepository, String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            rolRepository.save(new Rol(nombre));
        }
    }
}
