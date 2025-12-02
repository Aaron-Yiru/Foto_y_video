package pe.edu.uni.demospring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.uni.demospring.model.Usuario;
import pe.edu.uni.demospring.model.Rol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyUserPrincipal implements UserDetails {

    private final Usuario usuario;

    public MyUserPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    // Convertimos los Roles de la DB a permisos de Spring
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Si el usuario tiene roles asignados, los convertimos
        if (usuario.getRoles() != null) {
            for (Rol rol : usuario.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(rol.getNombre()));
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail(); // Usamos el EMAIL para loguearse
    }

    // Métodos helper para acceder a datos extra en el HTML
    public String getNombreCompleto() {
        return usuario.getNombre();
    }

    public Long getId() {
        return usuario.getId();
    }

    // Configuraciones de cuenta (por ahora todo true)
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return usuario.getActivo(); // Respetamos si el usuario fue desactivado en BD
    }
}
