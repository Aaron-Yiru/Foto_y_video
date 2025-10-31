package pe.edu.uni.demospring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir recursos estáticos de /static
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // Servir imágenes subidas en uploads/images + imágenes estáticas antiguas
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads/images");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "file:" + uploadPath + "/",      // nuevas imágenes subidas
                        "classpath:/static/images/"     // imágenes existentes en static/images
                );
    }
}
