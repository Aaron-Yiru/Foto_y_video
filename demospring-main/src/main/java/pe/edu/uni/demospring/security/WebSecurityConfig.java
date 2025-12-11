package pe.edu.uni.demospring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.uni.demospring.security.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final MyUserDetailsService userDetailsService;

    public WebSecurityConfig(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        //  RECURSOS ESTÁTICOS (Siempre públicos)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/videos/**", "/uploads/**").permitAll()

                        //  RUTAS PÚBLICAS (Visitantes)
                        .requestMatchers("/", "/index", "/servicios", "/galeria", "/contacto", "/contacto-form/**").permitAll()

                        //  RUTAS DE AUTENTICACIÓN
                        .requestMatchers("/perfil", "/perfil/registrar").permitAll()

                        //  ÁREA ADMINISTRATIVA (Solo ADMIN)
                        .requestMatchers("/admin/**", "/verContratos", "/gestionusuarios").hasRole("ADMIN")

                        //  ÁREA DE USUARIO LOGUEADO (Carrito, Perfil, etc.)
                        .requestMatchers("/perfil/**", "/carrito/**").authenticated()

                        // Cualquier otra cosa requiere login
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/perfil") // Usamos tu vista perfil-sesion.html como login
                        .loginProcessingUrl("/login-check") // URL interna donde Spring recibe el POST del form
                        .usernameParameter("email") // El name del input en tu HTML
                        .passwordParameter("password") // El name del input password
                        .defaultSuccessUrl("/perfil", true) // A dónde ir si el login es correcto
                        .failureUrl("/perfil?error=true") // A dónde ir si falla
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encriptación segura
    }
}
