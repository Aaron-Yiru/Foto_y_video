package pe.edu.uni.demospring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // Nueva Importación

import pe.edu.uni.demospring.interceptor.SessionInterceptor; // Nueva Importación (Ajusta si el paquete es diferente)

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 📂 Carpeta física donde se guardan las imágenes subidas
        Path uploadDir = Paths.get("uploads/images");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // 🔗 Exponer esa carpeta para que se pueda acceder vía /images/**
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar el SessionInterceptor
        registry.addInterceptor(new SessionInterceptor())

                .addPathPatterns("/**")

                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/videos/**",
                        "/favicon.ico",
                        "/uploads/**"
                );
    }
}
